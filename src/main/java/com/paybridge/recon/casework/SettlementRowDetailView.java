package com.paybridge.recon.casework;

import java.time.Instant;
import java.util.UUID;

public record SettlementRowDetailView(
        UUID settlementRowId,
        int rowNumber,
        String provider,
        String orderId,
        String providerPaymentId,
        String providerTransactionId,
        long amountMinor,
        String currency,
        Instant settledAt,
        String rawRowJson
) {
}
