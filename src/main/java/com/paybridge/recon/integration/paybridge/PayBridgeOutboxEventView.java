package com.paybridge.recon.integration.paybridge;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PayBridgeOutboxEventView(
        UUID id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String status,
        int retryCount,
        OffsetDateTime availableAt,
        OffsetDateTime publishedAt,
        String lastError,
        String payloadJson,
        OffsetDateTime createdAt
) {
}
