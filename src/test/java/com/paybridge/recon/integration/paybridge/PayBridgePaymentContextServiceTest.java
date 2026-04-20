package com.paybridge.recon.integration.paybridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class PayBridgePaymentContextServiceTest {

    @Mock
    private PayBridgeOpsClient payBridgeOpsClient;

    @InjectMocks
    private PayBridgePaymentContextService service;

    @Test
    void returnsUpstreamContextWhenAllCallsSucceed() {
        UUID paymentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        PayBridgePaymentDetailView detail = new PayBridgePaymentDetailView(
            paymentId,
            "ORD-2001",
            "STRIPE",
            "APPROVED",
            "$19.99",
            "$19.99",
            "USD",
            "pi_123",
            "ch_123",
            "2025-01-15 12:00:00 UTC",
            true,
            true,
            List.of()
        );
        PayBridgeAuditLogView auditLog = new PayBridgeAuditLogView(
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            "PAYMENT_APPROVED",
            "SUCCESS",
            "PAYMENT",
            paymentId.toString(),
            "STRIPE",
            "SYSTEM",
            "corr-123",
            "Payment approved.",
            "{}",
            OffsetDateTime.parse("2025-01-15T12:00:00Z")
        );
        PayBridgeOutboxEventView outboxEvent = new PayBridgeOutboxEventView(
            UUID.fromString("33333333-3333-3333-3333-333333333333"),
            "PAYMENT",
            paymentId.toString(),
            "PAYMENT_APPROVED",
            "PENDING",
            0,
            OffsetDateTime.parse("2025-01-15T12:00:01Z"),
            null,
            null,
            "{}",
            OffsetDateTime.parse("2025-01-15T12:00:01Z")
        );

        when(payBridgeOpsClient.fetchPaymentDetail(paymentId)).thenReturn(ResponseEntity.ok(detail));
        when(payBridgeOpsClient.fetchAuditLogs(paymentId)).thenReturn(ResponseEntity.ok(List.of(auditLog)));
        when(payBridgeOpsClient.fetchOutboxEvents(paymentId)).thenReturn(ResponseEntity.ok(List.of(outboxEvent)));

        PayBridgePaymentContextView view = service.load(paymentId);

        assertThat(view.paymentDetail()).isEqualTo(detail);
        assertThat(view.auditLogs()).containsExactly(auditLog);
        assertThat(view.outboxEvents()).containsExactly(outboxEvent);
        assertThat(view.errorSummary()).isNull();
    }

    @Test
    void keepsPartialResultsAndSummarizesFailures() {
        UUID paymentId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        PayBridgeAuditLogView auditLog = new PayBridgeAuditLogView(
            UUID.fromString("55555555-5555-5555-5555-555555555555"),
            "PAYMENT_APPROVED",
            "SUCCESS",
            "PAYMENT",
            paymentId.toString(),
            "STRIPE",
            "SYSTEM",
            "corr-999",
            "Payment approved.",
            null,
            OffsetDateTime.parse("2025-01-15T12:00:00Z")
        );

        when(payBridgeOpsClient.fetchPaymentDetail(paymentId)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));
        when(payBridgeOpsClient.fetchAuditLogs(paymentId)).thenReturn(ResponseEntity.ok(List.of(auditLog)));
        when(payBridgeOpsClient.fetchOutboxEvents(paymentId)).thenThrow(new ResourceAccessException("upstream timeout"));

        PayBridgePaymentContextView view = service.load(paymentId);

        assertThat(view.paymentDetail()).isNull();
        assertThat(view.auditLogs()).containsExactly(auditLog);
        assertThat(view.outboxEvents()).isEmpty();
        assertThat(view.errorSummary())
            .contains("payment detail")
            .contains("HTTP 404")
            .contains("outbox events")
            .contains("upstream timeout");
    }
}
