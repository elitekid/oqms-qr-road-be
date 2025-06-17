package com.qrroad.oqms.unionpay.dto;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

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
