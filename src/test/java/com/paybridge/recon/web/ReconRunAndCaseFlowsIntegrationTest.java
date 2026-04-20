package com.paybridge.recon.web;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.paybridge.recon.casework.ReconCaseJpaRepository;
import com.paybridge.recon.casework.ReconCaseNoteJpaRepository;
import com.paybridge.recon.casework.ReconCaseStatus;
import com.paybridge.recon.run.PayBridgeSnapshotJpaRepository;
import com.paybridge.recon.run.ReconRunJpaRepository;
import com.paybridge.recon.settlement.SettlementImportBatchJpaRepository;
import com.paybridge.recon.settlement.SettlementRowJpaRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReconRunAndCaseFlowsIntegrationTest {

    @RegisterExtension
    static WireMockExtension payBridge = WireMockExtension.newInstance()
        .options(com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig().dynamicPort())
        .build();

    @DynamicPropertySource
    static void payBridgeProperties(DynamicPropertyRegistry registry) {
        registry.add("paybridge-recon.integration.paybridge.base-url", payBridge::baseUrl);
        registry.add("paybridge-recon.integration.paybridge.operator-username", () -> "operator");
        registry.add("paybridge-recon.integration.paybridge.operator-password", () -> "operator-change-me");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SettlementImportBatchJpaRepository batchRepository;

    @Autowired
    private SettlementRowJpaRepository rowRepository;

    @Autowired
    private ReconRunJpaRepository reconRunJpaRepository;

    @Autowired
    private PayBridgeSnapshotJpaRepository payBridgeSnapshotJpaRepository;

    @Autowired
    private ReconCaseJpaRepository reconCaseJpaRepository;

    @Autowired
    private ReconCaseNoteJpaRepository reconCaseNoteJpaRepository;

    @BeforeEach
    void cleanRepositories() {
        reconCaseNoteJpaRepository.deleteAll();
        reconCaseJpaRepository.deleteAll();
        payBridgeSnapshotJpaRepository.deleteAll();
        reconRunJpaRepository.deleteAll();
        rowRepository.deleteAll();
        batchRepository.deleteAll();
        payBridge.resetAll();
    }

    @Test
    void createsManualRunSnapshotsAndDiscrepancyCases() throws Exception {
        stubExportPage();

        importSettlementCsv();
        var batch = batchRepository.findAll().getFirst();

        mockMvc.perform(post("/runs")
                .param("batchId", batch.getId().toString())
                .with(user("operator").roles("OPERATOR"))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/workbench/cases*"));

        assertThat(reconRunJpaRepository.count()).isEqualTo(1L);
        var run = reconRunJpaRepository.findAll().getFirst();
        assertThat(run.getStatus().name()).isEqualTo("COMPLETED");
        assertThat(run.getPaybridgeRowCount()).isEqualTo(3);
        assertThat(payBridgeSnapshotJpaRepository.findByRun_IdOrderByApprovedAtDesc(run.getId())).hasSize(3);
        assertThat(reconCaseJpaRepository.findAll()).hasSize(4);
        assertThat(reconCaseJpaRepository.findAll().stream().map(reconCase -> reconCase.getCaseType().name()).toList())
            .containsExactlyInAnyOrder("AMOUNT_MISMATCH", "DUPLICATE_SETTLEMENT_ROW", "SETTLEMENT_ONLY", "PAYBRIDGE_ONLY");
    }

    @Test
    void marksRunFailedWhenPayBridgeExportResponseIsMalformed() throws Exception {
        stubMalformedExportPage();

        importSettlementCsv();
        var batch = batchRepository.findAll().getFirst();

        mockMvc.perform(post("/runs")
                .param("batchId", batch.getId().toString())
                .with(user("operator").roles("OPERATOR"))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/runs/new"));

        assertThat(reconRunJpaRepository.count()).isEqualTo(1L);
        var run = reconRunJpaRepository.findAll().getFirst();
        assertThat(run.getStatus().name()).isEqualTo("FAILED");
        assertThat(run.getPaybridgeRowCount()).isEqualTo(0);
        assertThat(run.getCaseCount()).isEqualTo(0);
        assertThat(run.getErrorSummary()).contains("missing content");
    }

    @Test
    void exposesQueueAndDetailApisAndAllowsStatusAndNoteUpdates() throws Exception {
        stubExportPage();
        stubPayBridgeCaseContext();

        importSettlementCsv();
        var batch = batchRepository.findAll().getFirst();

        mockMvc.perform(post("/runs")
                .param("batchId", batch.getId().toString())
                .with(user("operator").roles("OPERATOR"))
                .with(csrf()))
            .andExpect(status().is3xxRedirection());

        var run = reconRunJpaRepository.findAll().getFirst();
        var caseId = reconCaseJpaRepository.findAll().stream()
            .filter(reconCase -> reconCase.getPaymentId() != null)
            .filter(reconCase -> "11111111-1111-1111-1111-111111111111".equals(reconCase.getPaymentId().toString()))
            .findFirst()
            .orElseThrow()
            .getId();

        mockMvc.perform(get("/api/recon/cases")
                .param("runId", run.getId().toString())
                .with(user("operator").roles("OPERATOR")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(4));

        mockMvc.perform(get("/api/recon/workbench/bootstrap")
                .with(user("operator").roles("OPERATOR")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.operatorName").value("operator"))
            .andExpect(jsonPath("$.recentRuns[0].runId").value(run.getId().toString()))
            .andExpect(jsonPath("$.caseStatuses[0]").value("OPEN"));

        mockMvc.perform(get("/api/recon/cases/{caseId}", caseId)
                .with(user("operator").roles("OPERATOR")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.caseId").value(caseId.toString()))
            .andExpect(jsonPath("$.payBridgeContext.paymentDetail.status").value("APPROVED"))
            .andExpect(jsonPath("$.payBridgeContext.auditLogs[0].action").value("PAYMENT_APPROVED"))
            .andExpect(jsonPath("$.payBridgeContext.outboxEvents[0].eventType").value("PAYMENT_APPROVED"));

        mockMvc.perform(patch("/api/recon/cases/{caseId}/status", caseId)
                .contentType("application/json")
                .content("{" + "\"status\":\"IN_REVIEW\"}")
                .with(user("operator").roles("OPERATOR"))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.caseStatus").value("IN_REVIEW"));

        mockMvc.perform(post("/api/recon/cases/{caseId}/notes", caseId)
                .contentType("application/json")
                .content("{" + "\"body\":\"Reviewed against the settlement batch.\"}")
                .with(user("operator").roles("OPERATOR"))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.author").value("operator"))
            .andExpect(jsonPath("$.body").value("Reviewed against the settlement batch."));

        var updatedCase = reconCaseJpaRepository.findById(caseId).orElseThrow();
        assertThat(updatedCase.getCaseStatus()).isEqualTo(ReconCaseStatus.IN_REVIEW);
        assertThat(reconCaseNoteJpaRepository.findByReconCase_IdOrderByCreatedAtAsc(caseId)).hasSize(1);
    }

    private void importSettlementCsv() throws Exception {
        String csv = "provider,order_id,provider_payment_id,provider_transaction_id,amount_minor,currency,settled_at\n"
            + "STRIPE,ORD-2001,pi_123,ch_123,2099,USD,2025-01-15T12:30:00Z\n"
            + "STRIPE,ORD-2002,pi_dup,ch_dup,500,USD,2025-01-15T12:31:00Z\n"
            + "STRIPE,ORD-2002,pi_dup,ch_dup,500,USD,2025-01-15T12:32:00Z\n"
            + "NICEPAY,ORD-2009,cp_999,TID-999,1000,KRW,2025-01-15T12:33:00Z\n";

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "settlement.csv",
            "text/csv",
            csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/imports")
                .file(file)
                .with(user("operator").roles("OPERATOR"))
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    private void stubExportPage() {
        payBridge.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/ops/transactions/export"))
            .willReturn(okJson("""
                {
                  "content": [
                    {
                      "paymentId": "11111111-1111-1111-1111-111111111111",
                      "orderId": "ORD-2001",
                      "provider": "STRIPE",
                      "status": "APPROVED",
                      "amountMinor": 1999,
                      "reversibleAmountMinor": 1999,
                      "currency": "USD",
                      "providerPaymentId": "pi_123",
                      "providerTransactionId": "ch_123",
                      "approvedAt": "2025-01-15T12:00:00Z",
                      "createdAt": "2025-01-15T11:59:58Z",
                      "updatedAt": "2025-01-15T12:00:01Z"
                    },
                    {
                      "paymentId": "22222222-2222-2222-2222-222222222222",
                      "orderId": "ORD-2002",
                      "provider": "STRIPE",
                      "status": "APPROVED",
                      "amountMinor": 500,
                      "reversibleAmountMinor": 500,
                      "currency": "USD",
                      "providerPaymentId": "pi_dup",
                      "providerTransactionId": "ch_dup",
                      "approvedAt": "2025-01-15T12:01:00Z",
                      "createdAt": "2025-01-15T12:00:58Z",
                      "updatedAt": "2025-01-15T12:01:01Z"
                    },
                    {
                      "paymentId": "33333333-3333-3333-3333-333333333333",
                      "orderId": "ORD-7777",
                      "provider": "STRIPE",
                      "status": "APPROVED",
                      "amountMinor": 700,
                      "reversibleAmountMinor": 700,
                      "currency": "USD",
                      "providerPaymentId": "pi_paybridge_only",
                      "providerTransactionId": "ch_paybridge_only",
                      "approvedAt": "2025-01-15T12:02:00Z",
                      "createdAt": "2025-01-15T12:01:58Z",
                      "updatedAt": "2025-01-15T12:02:01Z"
                    }
                  ],
                  "page": 0,
                  "size": 200,
                  "hasNext": false
                }
                """)));
    }

    private void stubMalformedExportPage() {
        payBridge.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/ops/transactions/export"))
            .willReturn(okJson("""
                {
                  "page": 0,
                  "size": 200,
                  "hasNext": false
                }
                """)));
    }

    private void stubPayBridgeCaseContext() {
        payBridge.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/ops/transactions/11111111-1111-1111-1111-111111111111"))
            .willReturn(okJson("""
                {
                  "paymentId": "11111111-1111-1111-1111-111111111111",
                  "orderId": "ORD-2001",
                  "provider": "STRIPE",
                  "status": "APPROVED",
                  "amountDisplay": "$19.99",
                  "reversibleAmountDisplay": "$19.99",
                  "currency": "USD",
                  "providerPaymentId": "pi_123",
                  "providerTransactionId": "ch_123",
                  "approvedAtDisplay": "2025-01-15 12:00:00 UTC",
                  "fullReversalAllowed": true,
                  "partialReversalAllowed": true,
                  "reversals": []
                }
                """)));

        payBridge.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/ops/transactions/11111111-1111-1111-1111-111111111111/audit-logs"))
            .willReturn(okJson("""
                [
                  {
                    "id": "44444444-4444-4444-4444-444444444444",
                    "action": "PAYMENT_APPROVED",
                    "outcome": "SUCCESS",
                    "resourceType": "PAYMENT",
                    "resourceId": "11111111-1111-1111-1111-111111111111",
                    "provider": "STRIPE",
                    "actorType": "SYSTEM",
                    "correlationId": "corr-123",
                    "message": "Payment approved.",
                    "detailJson": "{}",
                    "occurredAt": "2025-01-15T12:00:00Z"
                  }
                ]
                """)));

        payBridge.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/ops/transactions/11111111-1111-1111-1111-111111111111/outbox-events"))
            .willReturn(okJson("""
                [
                  {
                    "id": "55555555-5555-5555-5555-555555555555",
                    "aggregateType": "PAYMENT",
                    "aggregateId": "11111111-1111-1111-1111-111111111111",
                    "eventType": "PAYMENT_APPROVED",
                    "status": "PENDING",
                    "retryCount": 0,
                    "availableAt": "2025-01-15T12:00:01Z",
                    "publishedAt": null,
                    "lastError": null,
                    "payloadJson": "{}",
                    "createdAt": "2025-01-15T12:00:01Z"
                  }
                ]
                """)));
    }
}
