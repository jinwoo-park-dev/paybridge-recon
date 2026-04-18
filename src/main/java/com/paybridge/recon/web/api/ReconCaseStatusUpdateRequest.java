package com.paybridge.recon.web.api;

import com.paybridge.recon.casework.ReconCaseStatus;
import jakarta.validation.constraints.NotNull;

public record ReconCaseStatusUpdateRequest(
        @NotNull ReconCaseStatus status
) {
}
