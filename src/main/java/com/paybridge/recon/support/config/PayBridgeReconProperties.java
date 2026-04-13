package com.paybridge.recon.support.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paybridge-recon")
public class PayBridgeReconProperties {

    private final App app = new App();
    private final Security security = new Security();

    public App getApp() {
        return app;
    }

    public Security getSecurity() {
        return security;
    }

    public static class App {
        private String displayName = "PayBridge Recon";
        private String subtitle = "Settlement & Reconciliation Workbench";
        private String uiStrategy = "Spring MVC pages for imports, runs, and system plus a focused React case workbench";
        private String architectureStyle = "Companion reconciliation service";
        private boolean reactWorkbenchEnabled = true;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getUiStrategy() {
            return uiStrategy;
        }

        public void setUiStrategy(String uiStrategy) {
            this.uiStrategy = uiStrategy;
        }

        public String getArchitectureStyle() {
            return architectureStyle;
        }

        public void setArchitectureStyle(String architectureStyle) {
            this.architectureStyle = architectureStyle;
        }

        public boolean isReactWorkbenchEnabled() {
            return reactWorkbenchEnabled;
        }

        public void setReactWorkbenchEnabled(boolean reactWorkbenchEnabled) {
            this.reactWorkbenchEnabled = reactWorkbenchEnabled;
        }
    }

    public static class Security {
        private String operatorUsername;
        private String operatorPassword;
        private List<String> roles = new ArrayList<>(List.of("OPERATOR"));

        public String getOperatorUsername() {
            return operatorUsername;
        }

        public void setOperatorUsername(String operatorUsername) {
            this.operatorUsername = operatorUsername;
        }

        public String getOperatorPassword() {
            return operatorPassword;
        }

        public void setOperatorPassword(String operatorPassword) {
            this.operatorPassword = operatorPassword;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
