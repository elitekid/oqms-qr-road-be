package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.domain.dto.OqPaymentDto;
import com.qrroad.oqms.domain.repository.OqPaymentRepository;
import com.qrroad.oqms.unionpay.dto.ApiMsgDto;
import com.qrroad.oqms.unionpay.dto.UpiMsgDto;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.TrxInfo;
import com.qrroad.oqms.unionpay.enums.TransactionType;
import com.qrroad.oqms.unionpay.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OqPaymentService {
    private final OqPaymentRepository oqPaymentRepository;

    /**
     * QR 결제 데이터를 저장합니다.
     *
     * @param upiDto UPI 메시지 DTO
     * @param apiMsgDto API 메시지 DTO
     * @param trxType 거래 유형
     */
    public <T extends TrxInfo> void savePaymentData(UpiMsgDto<T> upiDto, ApiMsgDto apiMsgDto, TransactionType trxType) {
        try {
            // 공통 필드 설정
            OqPaymentDto.OqPaymentDtoBuilder builder = OqPaymentDto.builder()
                    .trxDt(upiDto.getMsgInfo().getTimeStamp().substring(0, 8))
                    .trxTm(upiDto.getMsgInfo().getTimeStamp().substring(8))
                    .txnId(getTransactionId(upiDto))
                    .instCd(apiMsgDto.getPayInfo().getInstCd())
                    .trxIn(trxType.getCode())
                    .msgId(upiDto.getMsgInfo().getMsgID())
                    .walletId(upiDto.getMsgInfo().getWalletID())
                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()));

            // 거래 유형별 특정 필드 설정
            setTypeSpecificData(builder, upiDto, trxType);

            // 응답 코드가 있으면 설정
            if (upiDto.getMsgResponse() != null) {
                builder.responseCode(upiDto.getMsgResponse().getResponseCode())
                       .responseMsg(upiDto.getMsgResponse().getResponseMsg());

                // 결제 상태 설정
                setPaymentStatus(builder, upiDto);
            }

            // 할인 정보 설정
            setDiscountDetails(builder, upiDto);

            // 저장 실행
            oqPaymentRepository.save(builder.build());
        } catch (Exception e) {
            log.error("QR 결제 데이터 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * QR 결제 데이터를 업데이트합니다.
     *
     * @param upiDto UPI 메시지 DTO
     */
    public <T extends TrxInfo> void updatePaymentData(UpiMsgDto<T> upiDto) {
        try {
            // 기본 키 생성
            OqPaymentDto.OqPaymentDtoBuilder builder = OqPaymentDto.builder()
                    .trxDt(upiDto.getMsgInfo().getTimeStamp().substring(0, 8))
                    .trxTm(upiDto.getMsgInfo().getTimeStamp().substring(8))
                    .txnId(getTransactionId(upiDto))
                    .updateTime(new Timestamp(System.currentTimeMillis()));

            // 응답 정보 설정
            if (upiDto.getMsgResponse() != null) {
                builder.responseCode(upiDto.getMsgResponse().getResponseCode())
                       .responseMsg(upiDto.getMsgResponse().getResponseMsg());
            }

            // 결제 상태 설정
            setPaymentStatus(builder, upiDto);
            
            // 거래 유형별 특정 필드 업데이트
            updateTypeSpecificData(builder, upiDto);
            
            // 할인 정보 설정
            setDiscountDetails(builder, upiDto);

            // 업데이트 실행
            oqPaymentRepository.save(builder.build());
        } catch (Exception e) {
            log.error("QR 결제 데이터 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 결제 상태를 설정합니다.
     */
    private <T extends TrxInfo> void setPaymentStatus(OqPaymentDto.OqPaymentDtoBuilder builder, UpiMsgDto<T> upiDto) {
        if (upiDto.getTrxInfo() != null) {
            // 필드 이름이 'paymentStatus'인 경우를 처리
            try {
                Map<String, Object> map = CommonUtil.convertToMap(upiDto.getTrxInfo());
                if (map.containsKey("paymentStatus")) {
                    builder.paymentStatus(map.get("paymentStatus").toString());
                } else if (upiDto.getMsgResponse() != null) {
                    // 응답 메시지를 기반으로 결제 상태 유추
                    builder.paymentStatus(upiDto.getMsgResponse().getResponseMsg().toUpperCase());
                }
            } catch (Exception e) {
                log.warn("결제 상태 설정 중 오류: {}", e.getMessage());
            }
        }
    }

    /**
     * 할인 정보를 설정합니다.
     */
    private <T extends TrxInfo> void setDiscountDetails(OqPaymentDto.OqPaymentDtoBuilder builder, UpiMsgDto<T> upiDto) {
        try {
            Map<String, Object> map = CommonUtil.convertToMap(upiDto.getTrxInfo());
            if (map.containsKey("discountDetails")) {
                Object discounts = map.get("discountDetails");
                if (discounts instanceof Object[]) {
                    Object[] discountArray = (Object[]) discounts;
                    for (int i = 0; i < discountArray.length && i < 5; i++) {
                        Map<String, Object> discountMap = CommonUtil.convertToMap(discountArray[i]);
                        
                        if (i == 0) {
                            builder.discountAmt1(new BigDecimal(discountMap.getOrDefault("discountAmt", "0").toString()))
                                   .discountNote1(discountMap.getOrDefault("discountNote", "").toString());
                        } else if (i == 1) {
                            builder.discountAmt2(new BigDecimal(discountMap.getOrDefault("discountAmt", "0").toString()))
                                   .discountNote2(discountMap.getOrDefault("discountNote", "").toString());
                        } else if (i == 2) {
                            builder.discountAmt3(new BigDecimal(discountMap.getOrDefault("discountAmt", "0").toString()))
                                   .discountNote3(discountMap.getOrDefault("discountNote", "").toString());
                        } else if (i == 3) {
                            builder.discountAmt4(new BigDecimal(discountMap.getOrDefault("discountAmt", "0").toString()))
                                   .discountNote4(discountMap.getOrDefault("discountNote", "").toString());
                        } else if (i == 4) {
                            builder.discountAmt5(new BigDecimal(discountMap.getOrDefault("discountAmt", "0").toString()))
                                   .discountNote5(discountMap.getOrDefault("discountNote", "").toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("할인 정보 설정 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 거래 식별자를 가져옵니다.
     */
    private <T extends TrxInfo> String getTransactionId(UpiMsgDto<T> upiDto) {
        try {
            Map<String, Object> map = CommonUtil.convertToMap(upiDto.getTrxInfo());
            if (map.containsKey("txnID")) {
                return map.get("txnID").toString();
            }
        } catch (Exception e) {
            log.warn("거래 식별자 획득 중 오류: {}", e.getMessage());
        }
        // 거래 ID가 없는 경우 생성
        return CommonUtil.generateTnxID();
    }

    /**
     * 거래 유형별 특정 데이터를 설정합니다.
     */
    private <T extends TrxInfo> void setTypeSpecificData(OqPaymentDto.OqPaymentDtoBuilder builder, UpiMsgDto<T> upiDto, TransactionType trxType) {
        try {
            Map<String, Object> map = CommonUtil.convertToMap(upiDto.getTrxInfo());
            
            // 거래 유형에 따른 처리
            switch (trxType) {
                case CPM:
                    setCpmData(builder, map);
                    break;
                case MPM_EMV:
                    setMpmEmvData(builder, map);
                    break;
                case MPM_URL_BACKEND:
                case MPM_URL_FRONTEND:
                    setMpmUrlData(builder, map);
                    break;
                case CANCEL:
                    setCancelData(builder, map);
                    break;
                default:
                    // 공통 필드 설정 (이미 많은 필드는 기본 생성 시 설정됨)
                    setCommonFields(builder, map);
                    break;
            }
            
            // 결제 관련 필드 설정
            setPaymentFields(builder, map);
            
            // 가맹점 정보 설정
            setMerchantInfo(builder, map);
            
            // 결제 정보 설정
            setSettlementInfo(builder, map);
            
        } catch (Exception e) {
            log.warn("거래 유형별 데이터 설정 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 거래 유형별 특정 데이터를 업데이트합니다.
     */
    private <T extends TrxInfo> void updateTypeSpecificData(OqPaymentDto.OqPaymentDtoBuilder builder, UpiMsgDto<T> upiDto) {
        try {
            Map<String, Object> map = CommonUtil.convertToMap(upiDto.getTrxInfo());
            
            // QR 페이로드 업데이트
            if (map.containsKey("mpqrcPayload")) {
                builder.qrPayload(map.get("mpqrcPayload").toString());
            }
            if (map.containsKey("emvCpqrcPayload")) {
                if (map.get("emvCpqrcPayload") instanceof Object[]) {
                    Object[] payloads = (Object[]) map.get("emvCpqrcPayload");
                    if (payloads.length > 0) {
                        builder.qrPayload(payloads[0].toString());
                    }
                }
            }
            if (map.containsKey("barcodeCpqrcPayload")) {
                builder.barcodeCpQrcPayload(map.get("barcodeCpqrcPayload").toString());
            }
            
            // 결제 정보 업데이트
            setPaymentFields(builder, map);
            
            // 결제 상태 관련 필드 업데이트
            if (map.containsKey("rejectionReason")) {
                builder.rejectionReason(map.get("rejectionReason").toString());
            }
            
        } catch (Exception e) {
            log.warn("거래 유형별 데이터 업데이트 중 오류: {}", e.getMessage());
        }
    }

    /**
     * CPM 관련 데이터를 설정합니다.
     */
    private void setCpmData(OqPaymentDto.OqPaymentDtoBuilder builder, Map<String, Object> map) {
        if (map.containsKey("emvCpqrcPayload")) {
            if (map.get("emvCpqrcPayload") instanceof Object[]) {
                Object[] payloads = (Object[]) map.get("emvCpqrcPayload");
                if (payloads.length > 0) {
                    builder.qrPayload(payloads[0].toString());
                }
            }
        }
        if (map.containsKey("barcodeCpqrcPayload")) {
            builder.barcodeCpQrcPayload(map.get("barcodeCpqrcPayload").toString());
        }
        if (map.containsKey("qrcVoucherNo")) {
            builder.qrcVoucherNo(map.get("qrcVoucherNo").toString());
        }
    }

    /**
     * MPM EMV 관련 데이터를 설정합니다.
     */
    private void setMpmEmvData(OqPaymentDto.OqPaymentDtoBuilder builder, Map<String, Object> map) {
        if (map.containsKey("mpqrcPayload")) {
            builder.qrPayload(map.get("mpqrcPayload").toString());
        }
    }

    /**
     * MPM URL 관련 데이터를 설정합니다.
     */
    private void setMpmUrlData(OqPaymentDto.OqPaymentDtoBuilder builder, Map<String, Object> map) {
        if (map.containsKey("mpqrcPayload")) {
            builder.qrPayload(map.get("mpqrcPayload").toString());
        }
        if (map.containsKey("token")) {
            builder.token(map.get("token").toString());
        }
        if (map.containsKey("deviceID")) {
            builder.deviceId(map.get("deviceID").toString());
        }
    }

    /**
     * 취소 거래 관련 데이터를 설정합니다.
     */
    private void setCancelData(OqPaymentDto.OqPaymentDtoBuilder builder, Map<String, Object> map) {
        if (map.containsKey("token")) {
            builder.token(map.get("token").toString());
        }
        if (map.containsKey("deviceID")) {
            builder.deviceId(map.get("deviceID").toString());
        }
        // 취소에 특화된 필드 설정
        builder.paymentStatus("CANCELLED");
    }

    /**
     * 공통 필드를 설정합니다.
     */
    private void setCommonFields(OqPaymentDto.OqPaymentDtoBuilder builder, Map<String, Object> map) {
        if (map.containsKey("token")) {
            builder.token(map.get("token").toString());
        }
        if (map.containsKey("deviceID")) {
            builder.deviceId(map.get("deviceID").toString());
        }
        if (map.containsKey("srcCorrelationId")) {
            builder.srcCorrelationId(map.get("srcCorrelationId").toString());
        }
        if (map.containsKey("srcDigitalCardId")) {
            builder.srcDigitalCardId(map.get("srcDigitalCardId").toString());
        }
        if (map.containsKey("payOrderInfo")) {
            builder.payOrderInfo(map.get("payOrderInfo").toString());
        }
    }

    /**
     * 결제 필드를 설정합니다.
     */
    private void setPaymentFields(OqPaymentDto.OqPaymentDtoBuilder builder, Map<String, Object> map) {
        if (map.containsKey("trxAmt") && map.get("trxAmt") != null) {
            try {
                builder.trxAmt(new BigDecimal(map.get("trxAmt").toString()));
            } catch (Exception e) {
                log.warn("trxAmt 변환 오류: {}", e.getMessage());
            }
        }
        if (map.containsKey("trxCurrency")) {
            builder.trxCurrency(map.get("trxCurrency").toString());
        }
        if (map.containsKey("originalAmount") && map.get("originalAmount") != null) {
            try {
                builder.originalAmount(new BigDecimal(map.get("originalAmount").toString()));
            } catch (Exception e) {
                log.warn("originalAmount 변환 오류: {}", e.getMessage());
            }
        }
        if (map.containsKey("trxFeeAmt") && map.get("trxFeeAmt") != null) {
            try {
                builder.trxFeeAmt(new BigDecimal(map.get("trxFeeAmt").toString()));
            } catch (Exception e) {
                log.warn("trxFeeAmt 변환 오류: {}", e.getMessage());
            }
        }
    }

    /**
     * 가맹점 정보를 설정합니다.
     */
    private void setMerchantInfo(OqPaymentDto.OqPaymentDtoBuilder builder, Map<String, Object> map) {
        if (map.containsKey("merchantID")) {
            builder.merchantId(map.get("merchantID").toString());
        }
        if (map.containsKey("merchantName")) {
            builder.merchantName(map.get("merchantName").toString());
        }
        if (map.containsKey("mcc")) {
            builder.mcc(map.get("mcc").toString());
        }
        if (map.containsKey("subMerchantId")) {
            builder.subMerchantId(map.get("subMerchantId").toString());
        }
        if (map.containsKey("subMerchantName")) {
            builder.subMerchantName(map.get("subMerchantName").toString());
        }
    }

    /**
     * 결제 정보를 설정합니다.
     */
    private void setSettlementInfo(OqPaymentDto.OqPaymentDtoBuilder builder, Map<String, Object> map) {
        // 결제 키 정보가 다른 객체에 중첩되어 있을 수 있음
        Map<String, Object> settlementMap = null;
        
        if (map.containsKey("settlementKey")) {
            settlementMap = CommonUtil.convertToMap(map.get("settlementKey"));
        } else {
            settlementMap = map; // 직접 맵에 있을 수도 있음
        }
        
        if (settlementMap.containsKey("acquirerIIN")) {
            builder.acquirerIin(settlementMap.get("acquirerIIN").toString());
        }
        if (settlementMap.containsKey("forwardingIIN")) {
            builder.forwardingIin(settlementMap.get("forwardingIIN").toString());
        }
        if (settlementMap.containsKey("systemTraceAuditNumber")) {
            builder.systemTraceAuditNumber(settlementMap.get("systemTraceAuditNumber").toString());
        }
        if (settlementMap.containsKey("transmissionDateTime")) {
            builder.transmissionDateTime(settlementMap.get("transmissionDateTime").toString());
        }
        
        // 참조 번호
        if (map.containsKey("retrievalReferenceNumber")) {
            builder.retrievalReferenceNumber(map.get("retrievalReferenceNumber").toString());
        }
    }
}
