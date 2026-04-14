package com.paybridge.recon.web.system;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "ReconSystemInfoResponse", description = "Runtime metadata returned by the paybridge-recon system info endpoint.")
public record SystemInfoResponse(
        @Schema(example = "paybridge-recon")
        String service,
        @Schema(example = "PayBridge Recon — Settlement & Reconciliation Workbench")
        String project,
        @Schema(example = "0.1.0")
        String releaseVersion,
        @Schema(example = "Spring MVC pages for imports, runs, and system plus a focused React case workbench")
        String frontendChoice,
        @Schema(example = "Companion reconciliation service")
        String architectureStyle,
        boolean reactWorkbenchEnabled,
        List<String> activeProfiles
) {
}
