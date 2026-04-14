package com.paybridge.recon.web.system;

import com.paybridge.recon.support.config.PayBridgeReconProperties;
import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SystemInfoViewFactory {

    private static final String RELEASE_VERSION = "0.1.0";

    private final Environment environment;
    private final PayBridgeReconProperties properties;

    public SystemInfoViewFactory(Environment environment, PayBridgeReconProperties properties) {
        this.environment = environment;
        this.properties = properties;
    }

    public SystemInfoResponse create() {
        String[] activeProfiles = environment.getActiveProfiles();
        var profiles = activeProfiles.length > 0
            ? Arrays.asList(activeProfiles)
            : Arrays.asList(environment.getDefaultProfiles());

        return new SystemInfoResponse(
            "paybridge-recon",
            properties.getApp().getDisplayName() + " — " + properties.getApp().getSubtitle(),
            RELEASE_VERSION,
            properties.getApp().getUiStrategy(),
            properties.getApp().getArchitectureStyle(),
            properties.getApp().isReactWorkbenchEnabled(),
            profiles
        );
    }
}
