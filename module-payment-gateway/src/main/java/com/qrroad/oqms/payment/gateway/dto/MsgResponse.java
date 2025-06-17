package com.qrroad.oqms.payment.gateway.dto;

import com.qrroad.oqms.payment.gateway.util.ToStringUtil;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MsgResponse {
    private String responseCode;
    private String responseMsg;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}