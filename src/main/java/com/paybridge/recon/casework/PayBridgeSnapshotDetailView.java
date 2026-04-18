package com.paybridge.recon.casework;

import java.time.Instant;
import java.util.UUID;

public record PayBridgeSnapshotDetailView(
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
