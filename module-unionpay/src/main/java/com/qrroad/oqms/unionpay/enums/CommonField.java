package com.qrroad.oqms.unionpay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonField {
    
    //파라미터 공통부 설정
    VERSIONNO("versionNo","1.0.0"),
    WALLETID("walletID","39990313");

    private final String field;
    private final String value;
}