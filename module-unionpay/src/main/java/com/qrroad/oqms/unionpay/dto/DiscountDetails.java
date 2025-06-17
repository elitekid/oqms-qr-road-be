package com.qrroad.oqms.unionpay.dto;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class DiscountDetails {
    private String discountAmt;
    private String discountNote;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}