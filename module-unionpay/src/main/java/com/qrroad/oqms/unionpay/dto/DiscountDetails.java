package com.qrroad.oqms.unionpay.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class DiscountDetails {
    private String discountAmt;
    private String discountNote;
}