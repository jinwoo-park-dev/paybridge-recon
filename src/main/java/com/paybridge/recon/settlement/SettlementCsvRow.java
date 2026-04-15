package com.paybridge.recon.settlement;

import java.time.Instant;
import java.util.Map;

public record SettlementCsvRow(
        int rowNumber,
        String provider,
        String orderId,
        String providerPaymentId,
        String providerTransactionId,
        long amountMinor,
        String currency,
        Instant settledAt,
        Map<String, String> rawRow
) {
}
