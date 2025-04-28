package com.qrroad.oqms.unionpay.dto.upitrxinfo;

import com.qrroad.oqms.unionpay.dto.DiscountDetails;
import com.qrroad.oqms.unionpay.dto.SettlementKey;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class TrxStatusNotificationTrxInfo implements TrxInfo {
    private String deviceID;
    private String token;
    private String srcDigitalCardId;
    private String txnID;
    private String[] emvCpqrcPayload;
    private String[] barcodeCpqrcPayload;
    private String srcCorrelationId;
    private String trxAmt;
    private String trxCurrency;
    private String trxSurcharge;
    private DiscountDetails[] discountDetails;
    private String originalAmount;
    private String merchantName;
    private String qrcVoucherNo;
    private String retrievalReferenceNumber;
    private String merchantID;
    private String mcc;
    private String cdhldrBillAmt;
    private String cdhldrBillCurrency;
    private String cdhldrBillConvRate;
    private SettlementKey settlementKey;
    private String paymentStatus;
    private String rejectionReason;
}
