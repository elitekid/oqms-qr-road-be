package com.qrroad.oqms.infrastructure.service;

import com.qrroad.oqms.domain.repository.OqPaymentRepository;
import com.qrroad.oqms.domain.dto.OqPaymentDto;
import com.qrroad.oqms.infrastructure.entity.OqPayment;
import com.qrroad.oqms.infrastructure.entity.OqPaymentPK;
import com.qrroad.oqms.infrastructure.repository.JpaOqPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OqPaymentRepositoryImpl implements OqPaymentRepository {
    private final JpaOqPaymentRepository jpaRepository;

    @Override
    public void save(OqPaymentDto dto) {
        try {
            // 복합키 생성
            OqPaymentPK id = OqPaymentPK.builder()
                    .trxDt(dto.getTrxDt())
                    .trxTm(dto.getTrxTm())
                    .txnId(dto.getTxnId())
                    .build();

            OqPayment oqpayment = jpaRepository.findById(id).orElse(null);

            // 엔티티 생성 및 매핑
            // OqPayment entity = OqPayment.builder()
            //         .oqPaymentPK(pk)
            //         .instCd(dto.getInstCd())
            //         .spayCoCd(dto.getSpayCoCd())
            //         .trxCl(dto.getTrxCl())
            //         .trxIn(dto.getTrxIn())
            //         .token(dto.getToken())
            //         .deviceId(dto.getDeviceId())
            //         .srcDigitalCardId(dto.getSrcDigitalCardId())
            //         .trxAmt(dto.getTrxAmt())
            //         .trxCurrency(dto.getTrxCurrency())
            //         .originalAmount(dto.getOriginalAmount())
            //         .trxFeeAmt(dto.getTrxFeeAmt())
            //         .qrPayload(dto.getQrPayload())
            //         .barcodeCpQrcPayload(dto.getBarcodeCpQrcPayload())
            //         .srcCorrelationId(dto.getSrcCorrelationId())
            //         .qrcVoucherNo(dto.getQrcVoucherNo())
            //         .couponInfo(dto.getCouponInfo())
            //         .retrievalReferenceNumber(dto.getRetrievalReferenceNumber())
            //         .merchantId(dto.getMerchantId())
            //         .merchantName(dto.getMerchantName())
            //         .mcc(dto.getMcc())
            //         .subMerchantName(dto.getSubMerchantName())
            //         .subMerchantId(dto.getSubMerchantId())
            //         .cdhldrBillAmt(dto.getCdhldrBillAmt())
            //         .cdhldrBillCurrency(dto.getCdhldrBillCurrency())
            //         .cdhldrBillConvRate(dto.getCdhldrBillConvRate())
            //         .acquirerIin(dto.getAcquirerIin())
            //         .forwardingIin(dto.getForwardingIin())
            //         .systemTraceAuditNumber(dto.getSystemTraceAuditNumber())
            //         .transmissionDateTime(dto.getTransmissionDateTime())
            //         .gps(dto.getGps())
            //         .simcard(dto.getSimcard())
            //         .appUserId(dto.getAppUserId())
            //         .usrEnrolDate(dto.getUsrEnrolDate())
            //         .captureMethod(dto.getCaptureMethod())
            //         .ipAddress(dto.getIpAddress())
            //         .reservedMobileNo(dto.getReservedMobileNo())
            //         .deviceType(dto.getDeviceType())
            //         .deviceScore(dto.getDeviceScore())
            //         .billNo(dto.getBillNo())
            //         .mobileNo(dto.getMobileNo())
            //         .storeLabel(dto.getStoreLabel())
            //         .loyaltyNumber(dto.getLoyaltyNumber())
            //         .referenceLabel(dto.getReferenceLabel())
            //         .customerLabel(dto.getCustomerLabel())
            //         .terminalLabel(dto.getTerminalLabel())
            //         .trxPurpose(dto.getTrxPurpose())
            //         .consumerEmail(dto.getConsumerEmail())
            //         .consumerAddress(dto.getConsumerAddress())
            //         .discountAmt1(dto.getDiscountAmt1())
            //         .discountNote1(dto.getDiscountNote1())
            //         .discountAmt2(dto.getDiscountAmt2())
            //         .discountNote2(dto.getDiscountNote2())
            //         .discountAmt3(dto.getDiscountAmt3())
            //         .discountNote3(dto.getDiscountNote3())
            //         .discountAmt4(dto.getDiscountAmt4())
            //         .discountNote4(dto.getDiscountNote4())
            //         .discountAmt5(dto.getDiscountAmt5())
            //         .discountNote5(dto.getDiscountNote5())
            //         .paymentStatus(dto.getPaymentStatus())
            //         .rejectionReason(dto.getRejectionReason())
            //         .responseCode(dto.getResponseCode())
            //         .responseMsg(dto.getResponseMsg())
            //         .payOrderInfo(dto.getPayOrderInfo())
            //         .msgId(dto.getMsgId())
            //         .walletId(dto.getWalletId())
            //         .updateTime(dto.getUpdateTime())
            //         .createTime(dto.getCreateTime())
            //         .build();

            // jpaRepository.save(entity);
        } catch (Exception e) {
            // 예외 처리 (로그 등)
        }
    }
}
