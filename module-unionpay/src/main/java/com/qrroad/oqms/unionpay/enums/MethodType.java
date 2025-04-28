package com.qrroad.oqms.unionpay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MethodType {
    TOKEN_REQUEST("token", "processTokenRequest", "/payment/token/request"),
    TOKEN_STATE_UPDATE("token", "processTokenStateUpdate","/payment/token/update"),

    UNKNOWN("unknown", "unknown","unknown");

    private final String serviceName;
    private final String methodName;
    private final String url;
}