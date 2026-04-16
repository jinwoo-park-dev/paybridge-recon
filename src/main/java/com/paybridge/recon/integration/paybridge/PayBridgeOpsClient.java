package com.paybridge.recon.integration.paybridge;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Component
public class PayBridgeOpsClient {

    private final RestClient restClient;

    public PayBridgeOpsClient(RestClient payBridgeRestClient) {
        this.restClient = payBridgeRestClient;
    }

    public ResponseEntity<PayBridgeSystemInfoView> fetchSystemInfo() {
        return restClient.get()
            .uri("/api/system/info")
            .retrieve()
            .toEntity(PayBridgeSystemInfoView.class);
    }

    public ResponseEntity<PayBridgeExportPageView> fetchTransactionsExport(PayBridgeExportRequest request) {
        return restClient.get()
            .uri(uriBuilder -> buildExportUri(uriBuilder, request))
            .retrieve()
            .toEntity(PayBridgeExportPageView.class);
    }

    public ResponseEntity<PayBridgePaymentDetailView> fetchPaymentDetail(UUID paymentId) {
        return restClient.get()
            .uri("/api/ops/transactions/{paymentId}", paymentId)
            .retrieve()
            .toEntity(PayBridgePaymentDetailView.class);
    }

    public ResponseEntity<List<PayBridgeAuditLogView>> fetchAuditLogs(UUID paymentId) {
        return restClient.get()
            .uri("/api/ops/transactions/{paymentId}/audit-logs", paymentId)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {
            });
    }

    public ResponseEntity<List<PayBridgeOutboxEventView>> fetchOutboxEvents(UUID paymentId) {
        return restClient.get()
            .uri("/api/ops/transactions/{paymentId}/outbox-events", paymentId)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {
            });
    }

    private URI buildExportUri(UriBuilder uriBuilder, PayBridgeExportRequest request) {
        UriBuilder builder = uriBuilder.path("/api/ops/transactions/export")
            .queryParam("page", request.page())
            .queryParam("size", request.size());
        if (request.approvedFrom() != null) {
            builder.queryParam("approvedFrom", request.approvedFrom());
        }
        if (request.approvedTo() != null) {
            builder.queryParam("approvedTo", request.approvedTo());
        }
        if (request.provider() != null && !request.provider().isBlank()) {
            builder.queryParam("provider", request.provider());
        }
        if (request.status() != null && !request.status().isBlank()) {
            builder.queryParam("status", request.status());
        }
        return builder.build();
    }
}
