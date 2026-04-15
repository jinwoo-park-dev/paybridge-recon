package com.paybridge.recon.settlement;

import java.time.Instant;
import java.util.UUID;

public record SettlementImportSummary(
        UUID batchId,
        String filename,
        int rowCount,
        String uploadedBy,
        Instant uploadedAt,
        SettlementImportBatchStatus status
) {
}
