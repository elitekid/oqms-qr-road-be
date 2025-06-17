package com.qrroad.oqms.unionpay.dto;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ApiMsgDto {
    private PayInfo payInfo;
    private TrxInfo trxInfo;
    private MsgResponse msgResponse;

    @Getter
    @Builder(toBuilder = true)
    public static class TrxInfo {
        private String token;
        private String txnID;
        private String deviceID;
        private String mpqrcPayload;
        private String payOrderInfo;
        private String trxAmt;
        private String trxCurrency;
        private DiscountDetails[] discountDetails;
        private String merchantName;
        private String trxFeeAmt;
        private String originalAmount;
        private String qrcVoucherNo;
        private String couponInfo;
        private String merchantID;
        private String mcc;
        private SettlementKey settlementKey;
        private RiskInfo riskInfo;

        private String pan;
        private String expiryDate;
        private String appUserID;
        private String tokenAction;
        private String tokenState;
        private String tokenExpiry;
        private String reservedMobileNo;

        private String[] emvCpqrcPayload;
        private String[] barcodeCpqrcPayload;
        private String paymentStatus;
        private String rejectionReason;

        private String retrievalReferenceNumber;

        private String qrcUseCase;
        private String upperLimitAmt;
        private String merchantCountry;
        private String merchantCity;
        private String postalCode;
        private String languagePreference;
        private String merchantNameAL;
        private String merchantCityAL;
        private String terminalID;
        private String qrCodeType;
        private String subMerchantName;

        @Override
        public String toString() {
            return ToStringUtil.toPrettyString(this);
        }
    }

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}
