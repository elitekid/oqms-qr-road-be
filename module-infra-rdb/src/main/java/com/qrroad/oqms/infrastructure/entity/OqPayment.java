package com.qrroad.oqms.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "oq_payment", schema = "qrbgw")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OqPayment {
    @EmbeddedId
    private OqPaymentPK oqPaymentPK;

    @Column(name = "inst_cd", length = 3)
    private String instCd;

    @Column(name = "spay_co_cd", length = 20)
    private String spayCoCd;

    @Column(name = "trx_cl", length = 2)
    private String trxCl;

    @Column(name = "trx_in", length = 2)
    private String trxIn;

    @Column(name = "token", length = 19)
    private String token;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "src_digital_card_id", length = 36)
    private String srcDigitalCardId;

    @Column(name = "trx_amt", precision = 13, scale = 2)
    private BigDecimal trxAmt;

    @Column(name = "trx_currency", length = 3)
    private String trxCurrency;

    @Column(name = "trx_surcharge", precision = 13, scale = 2)
    private BigDecimal trxSurcharge;

    @Column(name = "original_amount", precision = 13, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "trx_fee_amt", precision = 13, scale = 2)
    private BigDecimal trxFeeAmt;

    @Column(name = "wallet_id", length = 8)
    private String walletId;

    @Column(name = "qr_payload", length = 2048)
    private String qrPayload;

    @Column(name = "barcode_cp_qrc_payload", length = 2048)
    private String barcodeCpQrcPayload;

    @Column(name = "src_correlation_id", length = 256)
    private String srcCorrelationId;

    @Column(name = "qrc_voucher_no", length = 20)
    private String qrcVoucherNo;

    @Column(name = "coupon_info", length = 30)
    private String couponInfo;

    @Column(name = "retrieval_reference_number", length = 12)
    private String retrievalReferenceNumber;

    @Column(name = "merchant_id", length = 15)
    private String merchantId;

    @Column(name = "merchant_name", length = 40)
    private String merchantName;

    @Column(name = "mcc", length = 4)
    private String mcc;

    @Column(name = "sub_merchant_name", length = 40)
    private String subMerchantName;

    @Column(name = "sub_merchant_id", length = 15)
    private String subMerchantId;

    @Column(name = "cdhldr_bill_amt", precision = 13, scale = 2)
    private BigDecimal cdhldrBillAmt;

    @Column(name = "cdhldr_bill_currency", length = 3)
    private String cdhldrBillCurrency;

    @Column(name = "cdhldr_bill_conv_rate", precision = 9, scale = 4)
    private BigDecimal cdhldrBillConvRate;

    @Column(name = "acquirer_iin", length = 8)
    private String acquirerIin;

    @Column(name = "forwarding_iin", length = 8)
    private String forwardingIin;

    @Column(name = "system_trace_audit_number", length = 6)
    private String systemTraceAuditNumber;

    @Column(name = "transmission_date_time", length = 10)
    private String transmissionDateTime;

    @Column(name = "gps", length = 64)
    private String gps;

    @Column(name = "simcard", length = 200)
    private String simcard;

    @Column(name = "app_user_id", length = 64)
    private String appUserId;

    @Column(name = "usr_enrol_date", length = 6)
    private String usrEnrolDate;

    @Column(name = "capture_method", length = 64)
    private String captureMethod;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "reserved_mobile_no", length = 25)
    private String reservedMobileNo;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(name = "device_score", length = 1)
    private String deviceScore;

    @Column(name = "bill_no", length = 25)
    private String billNo;

    @Column(name = "mobile_no", length = 25)
    private String mobileNo;

    @Column(name = "store_label", length = 25)
    private String storeLabel;

    @Column(name = "loyalty_number", length = 25)
    private String loyaltyNumber;

    @Column(name = "reference_label", length = 25)
    private String referenceLabel;

    @Column(name = "customer_label", length = 25)
    private String customerLabel;

    @Column(name = "terminal_label", length = 25)
    private String terminalLabel;

    @Column(name = "trx_purpose", length = 25)
    private String trxPurpose;

    @Column(name = "consumer_email", length = 100)
    private String consumerEmail;

    @Column(name = "consumer_address", length = 100)
    private String consumerAddress;

    @Column(name = "discount_amt1", precision = 13, scale = 2)
    private BigDecimal discountAmt1;

    @Column(name = "discount_note1", length = 50)
    private String discountNote1;

    @Column(name = "discount_amt2", precision = 13, scale = 2)
    private BigDecimal discountAmt2;

    @Column(name = "discount_note2", length = 50)
    private String discountNote2;

    @Column(name = "discount_amt3", precision = 13, scale = 2)
    private BigDecimal discountAmt3;

    @Column(name = "discount_note3", length = 50)
    private String discountNote3;

    @Column(name = "discount_amt4", precision = 13, scale = 2)
    private BigDecimal discountAmt4;

    @Column(name = "discount_note4", length = 50)
    private String discountNote4;

    @Column(name = "discount_amt5", precision = 13, scale = 2)
    private BigDecimal discountAmt5;

    @Column(name = "discount_note5", length = 50)
    private String discountNote5;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Column(name = "rejection_reason", length = 100)
    private String rejectionReason;

    @Column(name = "pay_order_info", length = 2048)
    private String payOrderInfo;

    @Column(name = "msg_id", length = 50)
    private String msgId;

    @Column(name = "response_code", length = 2)
    private String responseCode;

    @Column(name = "response_msg", length = 100)
    private String responseMsg;

    @Column(name = "update_time")
    private Timestamp updateTime;

    @Column(name = "create_time")
    private Timestamp createTime;
}
