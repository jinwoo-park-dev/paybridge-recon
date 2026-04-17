package com.paybridge.recon.run;

import java.time.Instant;
import java.util.UUID;

public record ReconRunSummaryView(
        UUID runId,
        UUID batchId,
        String batchFilename,
        ReconRunStatus status,
        int settlementRowCount,
        int paybridgeRowCount,
        int caseCount,
        Instant startedAt,
        Instant finishedAt,
        String errorSummary
) {
}
