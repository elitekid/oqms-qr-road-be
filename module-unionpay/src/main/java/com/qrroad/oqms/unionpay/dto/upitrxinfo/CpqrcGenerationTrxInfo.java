package com.qrroad.oqms.unionpay.dto.upitrxinfo;

import com.qrroad.oqms.unionpay.dto.RiskInfo;
import lombok.*;

@Getter
@Builder(toBuilder = true)
@ToString
public class CpqrcGenerationTrxInfo implements TrxInfo {
    private String deviceID;
    private String token;
    private String trxLimit;
    private String cvmLimit;
    private String limitCurrency;
    private String cpqrcNo;
    private String[] emvCpqrcPayload;
    private String[] barcodeCpqrcPayload;
    private String couponInfo;
    private RiskInfo riskInfo;
    private String appDiscretData;
}
