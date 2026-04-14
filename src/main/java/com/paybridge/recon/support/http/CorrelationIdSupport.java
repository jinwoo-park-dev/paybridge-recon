package com.paybridge.recon.support.http;

import java.util.UUID;
import org.slf4j.MDC;

public final class CorrelationIdSupport {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private CorrelationIdSupport() {
    }

    public static String resolveOrGenerate(String incomingValue) {
        if (incomingValue == null || incomingValue.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return incomingValue.trim();
    }

    public static void bind(String correlationId) {
        MDC.put(MDC_KEY, correlationId);
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
