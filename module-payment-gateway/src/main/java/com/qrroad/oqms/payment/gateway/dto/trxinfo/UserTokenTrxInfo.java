package com.qrroad.oqms.payment.gateway.dto.trxinfo;

import com.qrroad.oqms.payment.gateway.util.ToStringUtil;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class UserTokenTrxInfo implements TrxInfo {
    private String deviceId;
    private String appUserId;
    private String tokenAction;
    private String userToken;
    private String tokenState;
    private String tokenExpiry;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}