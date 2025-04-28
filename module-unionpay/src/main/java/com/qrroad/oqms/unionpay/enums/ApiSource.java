package com.qrroad.oqms.unionpay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiSource {
    WOORI_BANK("woori", ""),
    HANA_BANK("hana", ""),
    UNION_PAY("upi", "https://umpstest.unionpayintl.com/umpsqrgw"),

    UNKNOWN("unknown", "unknown");

    private final String source;
    private final String url;
}