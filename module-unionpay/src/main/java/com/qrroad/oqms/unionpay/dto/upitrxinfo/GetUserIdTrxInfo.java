package com.qrroad.oqms.unionpay.dto.upitrxinfo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class GetUserIdTrxInfo implements TrxInfo {
    private String userAuthCode;
    private String appUserId;
}
