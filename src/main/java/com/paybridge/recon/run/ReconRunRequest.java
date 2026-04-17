package com.paybridge.recon.run;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record ReconRunRequest(
        @NotNull UUID batchId,
        Instant approvedFrom,
        Instant approvedTo
) {
}
