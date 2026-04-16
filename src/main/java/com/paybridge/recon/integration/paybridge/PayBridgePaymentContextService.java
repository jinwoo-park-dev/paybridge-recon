package com.paybridge.recon.integration.paybridge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class PayBridgePaymentContextService {

    private final PayBridgeOpsClient payBridgeOpsClient;

    public PayBridgePaymentContextService(PayBridgeOpsClient payBridgeOpsClient) {
        this.payBridgeOpsClient = payBridgeOpsClient;
    }

    public PayBridgePaymentContextView load(UUID paymentId) {
        List<String> failures = new ArrayList<>();

        PayBridgePaymentDetailView paymentDetail = fetchValue(
            () -> payBridgeOpsClient.fetchPaymentDetail(paymentId),
            ResponseEntity::getBody,
            "payment detail",
            failures
        );

        List<PayBridgeAuditLogView> auditLogs = fetchValue(
            () -> payBridgeOpsClient.fetchAuditLogs(paymentId),
            response -> response.getBody() != null ? response.getBody() : List.of(),
            "audit logs",
            failures
        );

        List<PayBridgeOutboxEventView> outboxEvents = fetchValue(
            () -> payBridgeOpsClient.fetchOutboxEvents(paymentId),
            response -> response.getBody() != null ? response.getBody() : List.of(),
            "outbox events",
            failures
        );

        String errorSummary = failures.isEmpty() ? null : String.join(" | ", failures);
        return new PayBridgePaymentContextView(
            paymentDetail,
            auditLogs != null ? auditLogs : List.of(),
            outboxEvents != null ? outboxEvents : List.of(),
            errorSummary
        );
    }

    private <T> T fetchValue(
            ResponseSupplier<T> supplier,
            java.util.function.Function<ResponseEntity<T>, T> extractor,
            String operation,
            List<String> failures) {
        try {
            ResponseEntity<T> response = supplier.get();
            return extractor.apply(response);
        } catch (RestClientResponseException ex) {
            failures.add("Failed to load " + operation + " from PayBridge: HTTP " + ex.getRawStatusCode() + " " + ex.getStatusText());
            return null;
        } catch (RestClientException ex) {
            failures.add("Failed to load " + operation + " from PayBridge: " + ex.getMessage());
            return null;
        }
    }

    @FunctionalInterface
    private interface ResponseSupplier<T> {
        ResponseEntity<T> get();
    }
}
