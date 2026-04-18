package com.paybridge.recon.web.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReconCaseNoteCreateRequest(
        @NotBlank @Size(max = 2000) String body
) {
}
