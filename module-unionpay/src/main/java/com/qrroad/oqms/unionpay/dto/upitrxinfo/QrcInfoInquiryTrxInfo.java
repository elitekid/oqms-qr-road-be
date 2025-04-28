package com.qrroad.oqms.unionpay.dto.upitrxinfo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class QrcInfoInquiryTrxInfo implements TrxInfo {
    private String deviceID;
    private String mpqrcPayload;
    private String txnID;
    private String trxAmt;
    private String trxCurrency;
    private String qrcUseCase;
    private String upperLimitAmt;
    private String merchantName;
    private String mcc;
    private String merchantCountry;
    private String merchantCity;
    private String postalCode;
    private String languagePreference;
    private String merchantNameAL;
    private String merchantCityAL;
    private String terminalID;
    private String qrCodeType;
    private String subMerchantName;
}
