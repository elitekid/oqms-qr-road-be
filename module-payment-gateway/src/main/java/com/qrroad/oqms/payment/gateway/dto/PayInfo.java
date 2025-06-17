package com.qrroad.oqms.payment.gateway.dto;

import com.qrroad.oqms.payment.gateway.util.ToStringUtil;

import lombok.*;

@Getter
@Builder(toBuilder = true)
public class PayInfo {
    private String trxCl;
    private String instCd;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}