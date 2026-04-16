package com.paybridge.recon.integration.paybridge;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PayBridgeAuditLogView(
        UUID id,
        String action,
        String outcome,
        String resourceType,
        String resourceId,
        String provider,
        String actorType,
        String correlationId,
        String message,
        String detailJson,
        OffsetDateTime occurredAt
) {
}
