package com.paybridge.recon.web.api;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record ReconRunCreateRequest(
        @NotNull UUID batchId,
        Instant approvedFrom,
        Instant approvedTo
) {
}
