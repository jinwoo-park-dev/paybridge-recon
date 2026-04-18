package com.paybridge.recon.casework;

import java.util.UUID;

public record ReconCaseFilters(
        UUID runId,
        ReconCaseStatus caseStatus,
        ReconCaseType caseType,
        String provider,
        String q
) {
}
