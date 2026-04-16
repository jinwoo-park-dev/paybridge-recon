package com.paybridge.recon.integration.paybridge;

import java.time.Instant;

public record PayBridgeExportRequest(
        Instant approvedFrom,
        Instant approvedTo,
        String provider,
        String status,
        int page,
        int size
) {

    public static PayBridgeExportRequest firstPage(Instant approvedFrom, Instant approvedTo) {
        return new PayBridgeExportRequest(approvedFrom, approvedTo, null, null, 0, 200);
    }
}
