package com.paybridge.recon.web.api;

import java.time.Instant;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp
) {
}
