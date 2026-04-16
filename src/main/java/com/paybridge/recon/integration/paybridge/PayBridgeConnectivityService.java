package com.paybridge.recon.integration.paybridge;

import com.paybridge.recon.support.http.CorrelationIdSupport;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class PayBridgeConnectivityService {

    private final PayBridgeOpsClient payBridgeOpsClient;

    public PayBridgeConnectivityService(PayBridgeOpsClient payBridgeOpsClient) {
        this.payBridgeOpsClient = payBridgeOpsClient;
    }

    public PayBridgeConnectionStatus checkConnection() {
        Instant checkedAt = Instant.now();
        try {
            ResponseEntity<PayBridgeSystemInfoView> response = payBridgeOpsClient.fetchSystemInfo();
            return PayBridgeConnectionStatus.connected(
                response.getStatusCode().value(),
                response.getHeaders().getFirst(CorrelationIdSupport.HEADER_NAME),
                checkedAt,
                response.getBody()
            );
        } catch (RestClientResponseException ex) {
            return PayBridgeConnectionStatus.failed(
                ex.getStatusCode().value(),
                ex.getResponseHeaders() != null
                    ? ex.getResponseHeaders().getFirst(CorrelationIdSupport.HEADER_NAME)
                    : null,
                ex.getStatusText(),
                checkedAt
            );
        } catch (RestClientException ex) {
            return PayBridgeConnectionStatus.failed(null, null, ex.getMessage(), checkedAt);
        }
    }
}
