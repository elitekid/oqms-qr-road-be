package com.qrroad.oqms.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OqUpiUserAuthDto {
    private final String token;
    private final String mpqrcUrl;
    private final String appUserId;
    private final String authCode;
    private final String authStts;
    private final String txnID;
    private final BigDecimal trxAmt;
    private final String nextParam;
}
