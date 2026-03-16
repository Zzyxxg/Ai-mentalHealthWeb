package com.example.mentalhealth.config;

import java.util.UUID;
import org.slf4j.MDC;

public class TraceIdFilter {

    private static final String TRACE_ID = "traceId";

    public static String currentTraceId() {
        String traceId = MDC.get(TRACE_ID);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().replace("-", "");
            MDC.put(TRACE_ID, traceId);
        }
        return traceId;
    }

    public static void clear() {
        MDC.remove(TRACE_ID);
    }
}
