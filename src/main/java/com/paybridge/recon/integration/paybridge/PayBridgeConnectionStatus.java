package com.paybridge.recon.integration.paybridge;

import java.time.Instant;

public record PayBridgeConnectionStatus(
        boolean connected,
        Integer httpStatus,
        String correlationId,
        String errorSummary,
        Instant checkedAt,
        PayBridgeSystemInfoView systemInfo
) {

    public static PayBridgeConnectionStatus connected(
            Integer httpStatus,
            String correlationId,
            Instant checkedAt,
            PayBridgeSystemInfoView systemInfo) {
        return new PayBridgeConnectionStatus(true, httpStatus, correlationId, null, checkedAt, systemInfo);
    }

    public static PayBridgeConnectionStatus failed(
            Integer httpStatus,
            String correlationId,
            String errorSummary,
            Instant checkedAt) {
        return new PayBridgeConnectionStatus(false, httpStatus, correlationId, errorSummary, checkedAt, null);
    }
}
