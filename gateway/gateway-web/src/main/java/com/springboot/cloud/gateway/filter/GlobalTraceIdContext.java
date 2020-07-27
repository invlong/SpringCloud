package com.springboot.cloud.gateway.filter;

import java.util.UUID;

public class GlobalTraceIdContext {

    private static ThreadLocal<String> requestIdT = new ThreadLocal<>();
    public static final String REQUESTID_HEADER_KEY = "request-context-id";

    public static String getRequestId() {
        return requestIdT.get();
    }

    public static void setRequestId(String requestId) {
        requestIdT.set(requestId);
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-","");
    }
}
