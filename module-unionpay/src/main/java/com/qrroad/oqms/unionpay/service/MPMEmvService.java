package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.domain.dto.OqPaymentDto;
import com.qrroad.oqms.domain.repository.OqUpiTokenRepository;
import com.qrroad.oqms.unionpay.dto.*;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.MpmTrxInfo;
import com.qrroad.oqms.unionpay.enums.ApiSource;
import com.qrroad.oqms.unionpay.enums.MessageId;
import com.qrroad.oqms.unionpay.enums.MessageType;
import com.qrroad.oqms.unionpay.enums.TransactionType;
import com.qrroad.oqms.unionpay.keymanagement.UmpsCertificateKeyManager;
import com.qrroad.oqms.unionpay.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MPMEmvService {

    private final OqPaymentService oqPaymentService;
    private final OqUpiTokenRepository oqUpiTokenRepository;
    private final UmpsCertificateKeyManager umpsCertificateKeyManager;

    /**
     * 트랜잭션 결과를 처리하여 은행 응답 DTO를 생성합니다.
     *
     * @param apiMsgDto 은행으로부터의 요청 데이터
     * @return 처리된 결과 데이터
     */
    @Transactional
    public ApiMsgDto processMpqrcPaymentEmv(ApiMsgDto apiMsgDto) {
        // 0. UPI 요청 객체 생성
        UpiMsgDto<MpmTrxInfo> upiDto = generateMpqrcPaymentEmvRequestUpiDto(apiMsgDto);

        // 1. QR 결제내역 생성
        oqPaymentService.savePaymentData(upiDto, apiMsgDto, TransactionType.MPM_EMV);

        // 2. To-Be-Signed String 변환 (서명할 값)
        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiDto);

        // 3. 서명 / 요청 보낼 값으로 변환
        upiDto = upiDto.toBuilder()
                .certificateSignature(
                        upiDto.getCertificateSignature()
                                .toBuilder()
                                .signature(CommonUtil.signUmps(
                                        Objects.requireNonNull(jsonBody).getBytes(),
                                        umpsCertificateKeyManager.getPrivateKeys().getAppPrivateSignature2048()
                                ))
                                .build()
                )
                .build();

        String finalJsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiDto);

        // 4. UPI Api 호출 및 응답 수신
        UpiMsgDto<MpmTrxInfo> upiResponseDto = mpqrcPaymentEmv(finalJsonBody);

        // 5. 서명 검증
        CommonUtil.verifyUmps(upiResponseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        // 6. 결제내역 업데이트
        oqPaymentService.updatePaymentData(upiResponseDto);

        // 7. 은행 응답 DTO 생성 및 반환
        return generateMpqrcPaymentEmvRequestBankDto(upiResponseDto);
    }

    /**
     * 1. UPI 요청 객체 생성
     *
     * @param apiDto 은행으로부터의 요청 데이터
     * @return 생성된 UPI 요청 DTO
     */
    public UpiMsgDto<MpmTrxInfo> generateMpqrcPaymentEmvRequestUpiDto(ApiMsgDto apiDto) {
        MsgInfo msgInfo = CommonUtil.createMsgInfo(MessageId.A, MessageType.MPQRC_PAYMENT_EMV);

        Map<String, String> payloadMap = CommonUtil.parseTLV(apiDto.getTrxInfo().getMpqrcPayload());
        String trxAmt = apiDto.getTrxInfo().getTrxAmt();
        String trxFeeAmt = null;

        if (payloadMap.containsKey("54")) {
            trxAmt = payloadMap.get("54");
        }

        if (payloadMap.containsKey("56")) {
            float trxAmtFloat = Float.parseFloat(trxAmt);
            float convenienceFeeFloat = Float.parseFloat(payloadMap.get("56"));
            trxAmtFloat += convenienceFeeFloat;
            trxAmt = String.valueOf(trxAmtFloat);
            trxFeeAmt = payloadMap.get("56");
        }

        Map<String, String> additionalDataMap = getAdditionalDataInPayload(apiDto.getTrxInfo().getMpqrcPayload());
        MpmTrxInfo.AdditionalData additionalData = MpmTrxInfo.AdditionalData.builder()
                .billNo(additionalDataMap.get("01"))
                .mobileNo(additionalDataMap.get("02"))
                .storeLabel(additionalDataMap.get("03"))
                .loyaltyNumber(additionalDataMap.get("04"))
                .referenceLabel(additionalDataMap.get("05"))
                .customerLabel(additionalDataMap.get("06"))
                .terminalLabel(additionalDataMap.get("07"))
                .trxPurpose(additionalDataMap.get("08"))
                .consumerEmail(additionalDataMap.get("09"))
                .consumerAddress(additionalDataMap.get("09"))
                .build();

        RiskInfo riskInfo = RiskInfo.builder().build();

        MpmTrxInfo trxInfo = MpmTrxInfo.builder()
                .token(apiDto.getTrxInfo().getToken())
                .deviceID(oqUpiTokenRepository.getDeviceId(apiDto.getTrxInfo().getToken()))
                .txnID(CommonUtil.generateTnxID())
                .trxAmt(trxAmt)
                .trxFeeAmt(trxFeeAmt)
                .mpqrcPayload(apiDto.getTrxInfo().getMpqrcPayload())
                .couponInfo(apiDto.getTrxInfo().getCouponInfo())
                .riskInfo(riskInfo)
                .additionalData(additionalData)
                .build();

        //certificateSignature 생성
        CertificateSignature certificateSignature = CertificateSignature.builder()
                .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
                .signature("00000000")
                .build();

        return UpiMsgDto.<MpmTrxInfo>builder()
                .msgInfo(msgInfo)
                .trxInfo(trxInfo)
                .certificateSignature(certificateSignature)
                .build();
    }

    /**
     * 2. UPI 송신 : QR 결제 요청
     *
     * @param requestUpiDto UPI 요청 DTO
     * @return 응답 DTO
     */
    public UpiMsgDto<MpmTrxInfo> mpqrcPaymentEmv(String requestUpiDto) {
        return RestClient.create()
                .post()
                .uri(ApiSource.UNION_PAY.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestUpiDto)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * 3. Api 응답 객체 생성
     *
     * @param upiDto UPI 응답 DTO
     * @return 생성된 은행 응답 DTO
     */
    public ApiMsgDto generateMpqrcPaymentEmvRequestBankDto(UpiMsgDto<MpmTrxInfo> upiDto) {
        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
                .token(upiDto.getTrxInfo().getToken())
                .txnID(upiDto.getTrxInfo().getTxnID())
                .trxAmt(upiDto.getTrxInfo().getTrxAmt())
                .trxCurrency(upiDto.getTrxInfo().getTrxCurrency())
                .discountDetails(upiDto.getTrxInfo().getDiscountDetails())
                .originalAmount(upiDto.getTrxInfo().getOriginalAmount())
                .trxFeeAmt(upiDto.getTrxInfo().getTrxFeeAmt())
                .mpqrcPayload(upiDto.getTrxInfo().getMpqrcPayload())
                .qrcVoucherNo(upiDto.getTrxInfo().getQrcVoucherNo())
                .couponInfo(upiDto.getTrxInfo().getCouponInfo())
                .merchantID(upiDto.getTrxInfo().getMerchantID())
                .mcc(upiDto.getTrxInfo().getMcc())
                .settlementKey(upiDto.getTrxInfo().getSettlementKey())
                .build();

        return ApiMsgDto.builder()
                .trxInfo(trxInfo)
                .msgResponse(upiDto.getMsgResponse())
                .build();
    }

    /**
     * 4. OqPayment Dto 생성
     *
     */
    public OqPaymentDto generateOqPaymentDto(UpiMsgDto<MpmTrxInfo> upiDto, ApiMsgDto apiMsgDto) {
        return OqPaymentDto.builder()
                .trxDt(upiDto.getMsgInfo().getTimeStamp().substring(0,8))
                .trxTm(upiDto.getMsgInfo().getTimeStamp().substring(8))
                .txnId(upiDto.getTrxInfo().getTxnID())
                .instCd(apiMsgDto.getPayInfo().getInstCd())
                .trxIn(TransactionType.MPM_EMV.getCode())
                .qrPayload(upiDto.getTrxInfo().getMpqrcPayload())
                .msgId(upiDto.getMsgInfo().getMsgID())
                .build();
    }

    /**
     * 페이로드에서 추가 데이터를 가져옵니다.
     * @param payload 페이로드 문자열
     * @return 추가 데이터가 포함된 맵
     */
    public Map<String, String> getAdditionalDataInPayload(String payload) {
        Map<String, String> payloadMap = CommonUtil.parseTLV(payload);
        if (payloadMap.containsKey("62")) {
            return CommonUtil.parseTLV(payloadMap.get("62"));
        } else {
            return Collections.emptyMap();
        }
    }
}