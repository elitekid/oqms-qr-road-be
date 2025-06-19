package com.qrroad.oqms.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class OqUpiTokenDto {

    // 복합 키
    private final String instCd;
    private final String deviceId;

    private final String token;
    private final String tokenRefId;
    private final String tokenState;
    private final String tokenExpiry;
    private final String pan;
    private final String appUserId;
    private final String reservedMobileNo;

    private final Timestamp updateTime;
    private final Timestamp createTime;
}
