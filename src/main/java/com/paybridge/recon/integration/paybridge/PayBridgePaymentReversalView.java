package com.paybridge.recon.integration.paybridge;

import java.util.UUID;

public record PayBridgePaymentReversalView(
        UUID reversalId,
        String reversalType,
        String status,
        String amountDisplay,
        String remainingAmountDisplay,
        String reason,
        String providerReversalId,
        String processedAtDisplay
) {
}
