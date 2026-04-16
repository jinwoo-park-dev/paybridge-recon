package com.paybridge.recon.integration.paybridge;

import java.time.Instant;
import java.util.UUID;

public record PayBridgeExportRowView(
        UUID paymentId,
        String orderId,
        String provider,
        String status,
        long amountMinor,
        long reversibleAmountMinor,
        String currency,
        String providerPaymentId,
        String providerTransactionId,
        Instant approvedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
