package com.qrroad.oqms.unionpay.dto;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

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
