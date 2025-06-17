package com.qrroad.oqms.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder(toBuilder = true)
public class OqUserTokenDto {

    // 복합 키
    private final String userToken;
    private final String instCd;

    private final String deviceId;
    private final String appUserId;
    private final String tokenState;
    private final String tokenExpiry;

    private final Timestamp updateTime;
    private final Timestamp createTime;
}
