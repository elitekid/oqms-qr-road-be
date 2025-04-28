package com.qrroad.oqms.unionpay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceType {
    TOKEN("token"),
    MPM("mpm"),
    CPM("cpm");

    private final String serviceName;
}
