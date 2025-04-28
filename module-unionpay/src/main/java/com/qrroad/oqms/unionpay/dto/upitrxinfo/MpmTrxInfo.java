package com.qrroad.oqms.unionpay.dto.upitrxinfo;

import com.qrroad.oqms.unionpay.dto.DiscountDetails;
import com.qrroad.oqms.unionpay.dto.RiskInfo;
import com.qrroad.oqms.unionpay.dto.SettlementKey;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class MpmTrxInfo implements TrxInfo {
    private String deviceID;
    private String token;
    private String txnID;
    private String trxAmt;
    private String trxCurrency;
    private DiscountDetails[] discountDetails;
    private String originalAmount;
    private String merchantName;
    private String trxFeeAmt;
    private String mpqrcPayload;
    private String qrcVoucherNo;
    private String couponInfo;
    private String retrievalReferenceNumber;
    private String merchantID;
    private String mcc;
    private String subMerchantName;
    private String subMerchantID;
    private String cdhldrBillAmt;
    private String cdhldrBillCurrency;
    private String cdhldrBillConvRate;
    private SettlementKey settlementKey;
    private RiskInfo riskInfo;
    private AdditionalData additionalData;

    @Getter
    @Builder(toBuilder = true)
    public static class AdditionalData {
        private String billNo;
        private String mobileNo;
        private String storeLabel;
        private String loyaltyNumber;
        private String referenceLabel;
        private String customerLabel;
        private String terminalLabel;
        private String trxPurpose;
        private String consumerEmail;
        private String consumerAddress;
    }
}
