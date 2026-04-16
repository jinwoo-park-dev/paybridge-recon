package com.paybridge.recon.integration.paybridge;

import java.util.List;
import java.util.UUID;

public record PayBridgePaymentDetailView(
        UUID paymentId,
        String orderId,
        String provider,
        String status,
        String amountDisplay,
        String reversibleAmountDisplay,
        String currency,
        String providerPaymentId,
        String providerTransactionId,
        String approvedAtDisplay,
        boolean fullReversalAllowed,
        boolean partialReversalAllowed,
        List<PayBridgePaymentReversalView> reversals
) {
}
