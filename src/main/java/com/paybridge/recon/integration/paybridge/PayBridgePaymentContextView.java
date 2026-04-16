package com.paybridge.recon.integration.paybridge;

import java.util.List;

public record PayBridgePaymentContextView(
        PayBridgePaymentDetailView paymentDetail,
        List<PayBridgeAuditLogView> auditLogs,
        List<PayBridgeOutboxEventView> outboxEvents,
        String errorSummary
) {

    public static PayBridgePaymentContextView unavailable(String errorSummary) {
        return new PayBridgePaymentContextView(null, List.of(), List.of(), errorSummary);
    }
}
