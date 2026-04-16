package com.paybridge.recon.integration.paybridge;

import java.util.List;

public record PayBridgeSystemInfoView(
        String service,
        String project,
        String releaseVersion,
        String frontendChoice,
        String architectureStyle,
        boolean unifiedCheckoutEnabled,
        boolean operatorApiEnabled,
        List<String> activeProfiles
) {
}
