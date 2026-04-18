package com.paybridge.recon.casework;

import java.time.Instant;
import java.util.UUID;

public record ReconCaseNoteView(
        UUID noteId,
        String author,
        String body,
        Instant createdAt
) {
}
