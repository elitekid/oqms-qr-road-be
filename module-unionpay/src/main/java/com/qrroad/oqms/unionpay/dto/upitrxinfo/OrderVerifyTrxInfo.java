package com.qrroad.oqms.unionpay.dto.upitrxinfo;

import com.qrroad.oqms.unionpay.dto.DiscountDetails;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class OrderVerifyTrxInfo implements TrxInfo {
    private String txnID;
    private String payOrderInfo;
    private String deviceID;
    private String token;
    private String mobileNo;
    private String appUserID;
    private String qrcUseCase;
    private String trxAmt;
    private String trxCurrency;
    private String acquirerIIn;
    private String merchantID;
    private String merchantName;
    private String mcc;
    private String terminalID;
    private DiscountDetails[] discountDetails;
    private String umpsSignCertID;
    private String umpsSignPublicKey;
    private String appSignCertID;
    private String appSignPublicKey;
    private String umpsEncCertID;
    private String umpsEncPublicKey;
    private String appEncCertID;
    private String appEncPublicKey;
}
