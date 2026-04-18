package com.paybridge.recon.casework;

import java.time.Instant;
import java.util.UUID;

public record ReconCaseStatusUpdateView(
        UUID caseId,
        ReconCaseStatus caseStatus,
        Instant resolvedAt
) {
}
