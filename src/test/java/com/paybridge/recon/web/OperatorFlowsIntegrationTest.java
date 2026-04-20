package com.paybridge.recon.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paybridge.recon.casework.ReconCaseJpaRepository;
import com.paybridge.recon.casework.ReconCaseNoteJpaRepository;
import com.paybridge.recon.run.PayBridgeSnapshotJpaRepository;
import com.paybridge.recon.run.ReconRunJpaRepository;
import com.paybridge.recon.settlement.SettlementImportBatchJpaRepository;
import com.paybridge.recon.settlement.SettlementRowJpaRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperatorFlowsIntegrationTest {

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
    }

    @Test
    void exposesSystemInfoWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/system/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("paybridge-recon"))
            .andExpect(jsonPath("$.project").value("PayBridge Recon — Settlement & Reconciliation Workbench"));
    }


    @Test
    void rendersReactWorkbenchShellForAuthenticatedOperator() throws Exception {
        mockMvc.perform(get("/workbench/cases")
                .with(user("operator").roles("OPERATOR")))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("recon-workbench-root")))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("/workbench/main.js")));
    }

    @Test
    void importsSettlementCsvAndPersistsBatchAndRows() throws Exception {
        String csv = "provider,order_id,provider_payment_id,provider_transaction_id,amount_minor,currency,settled_at\n"
            + "STRIPE,ORD-2001,pi_123,ch_123,1999,USD,2025-01-15T12:30:00Z\n"
            + "NICEPAY,ORD-2002,cp_456,TID-1001,10004,KRW,2025-01-15T12:31:00Z\n";

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
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/imports"));

        assertThat(batchRepository.count()).isEqualTo(1L);
        var batch = batchRepository.findAll().getFirst();
        assertThat(batch.getFilename()).isEqualTo("settlement.csv");
        assertThat(batch.getRowCount()).isEqualTo(2);
        assertThat(batch.getUploadedBy()).isEqualTo("operator");
        assertThat(rowRepository.findByBatch_IdOrderByRowNumberAsc(batch.getId())).hasSize(2);
    }
}
