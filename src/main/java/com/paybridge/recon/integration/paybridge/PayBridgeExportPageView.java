package com.paybridge.recon.integration.paybridge;

import java.util.List;

public record PayBridgeExportPageView(
        List<PayBridgeExportRowView> content,
        int page,
        int size,
        boolean hasNext
) {
}
