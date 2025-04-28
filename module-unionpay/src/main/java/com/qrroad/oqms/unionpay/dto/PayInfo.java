package com.qrroad.oqms.unionpay.dto;

import lombok.*;

@Getter
@Builder(toBuilder = true)
@ToString
public class PayInfo {
    private String trxCl;
    private String instCd;
}
