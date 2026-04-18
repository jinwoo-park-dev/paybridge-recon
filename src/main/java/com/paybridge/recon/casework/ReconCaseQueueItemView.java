package com.paybridge.recon.casework;

import java.time.Instant;
import java.util.UUID;

public record ReconCaseQueueItemView(
        UUID caseId,
        UUID runId,
        ReconCaseType caseType,
        ReconCaseStatus caseStatus,
        String provider,
        String summary,
        String matchKey,
        UUID paymentId,
        UUID settlementRowId,
        Instant openedAt,
        Instant resolvedAt
) {
}
