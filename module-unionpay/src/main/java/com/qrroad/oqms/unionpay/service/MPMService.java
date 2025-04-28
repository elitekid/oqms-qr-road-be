package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.domain.dto.OqUpiTokenDto;
import com.qrroad.oqms.domain.repository.OqUpiTokenRepository;
import com.qrroad.oqms.unionpay.dto.*;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.MpmTrxInfo;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.TrxStatusNotificationTrxInfo;
import com.qrroad.oqms.unionpay.enums.ApiSource;
import com.qrroad.oqms.unionpay.enums.MessageId;
import com.qrroad.oqms.unionpay.enums.MessageType;
import com.qrroad.oqms.unionpay.keymanagement.UmpsCertificateKeyManager;
import com.qrroad.oqms.unionpay.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MPMService {

    private final UmpsCertificateKeyManager umpsCertificateKeyManager;
    private final OqUpiTokenRepository oqUpiTokenRepository;
    private final QrPayService qrPayService;
    private final TokenService tokenService;

    /**
     * 거래결과를 조회하여 은행 응답 DTO를 생성합니다.
     *
     * @param apiMsgDto 은행으로부터의 요청 데이터
     * @return 처리된 결과 데이터
     */
    @Transactional
    public ApiMsgDto processTrxResultInquiry(ApiMsgDto apiMsgDto) {
        // 0. 은행 DTO로부터 UPI 요청 DTO 생성
        UpiMsgDto<TrxStatusNotificationTrxInfo> upiDto = generateTrxResultInquiryUpiDto(apiMsgDto);

        // 1. To-Be-Signed String 변환 (서명할 값)
        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiDto);

        // 2. 서명 / 요청 보낼 값으로 변환
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
        log.info("TRX_RESULT_INQUIRY Request : {}", finalJsonBody);

        // 3. UPI 요청 DTO를 API에 전송하고 응답 DTO 받기
        UpiMsgDto<TrxStatusNotificationTrxInfo> upiResponseDto = trxResultInquiry(finalJsonBody);
        log.info("TRX_RESULT_INQUIRY Response : {}", upiResponseDto);

        // 4. 서명 검증
        CommonUtil.verifyUmps(upiResponseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        // 5. UPI 응답 DTO로부터 응답 DTO 생성 및 반환
        return generateTrxResultInquiryApiDto(upiResponseDto);
    }

    /**
     * MPM Back-End, Front-End 에서 사용하는 MPM-URL QR 결제 요청을 처리합니다.
     *
     * @param apiDto 은행으로부터 받은 요청 DTO
     * @return 응답 DTO
     */
    @Transactional
    public ResponseEntity<?> processMpqrcPaymentUrl(ApiMsgDto apiDto) {
        // 0. 요청 DTO 생성
        UpiMsgDto<MpmTrxInfo> upiDto = generateMpqrcPaymentUrlRequestUpiDto(apiDto);

        // 1. To-Be-Signed String 변환 (서명할 값)
        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiDto);

        // 2. 서명 / 요청 보낼 값으로 변환
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
        log.info("MPQRC_PAYMENT_URL Request : {}", upiDto);

        // 3. UPI 요청 DTO를 API에 전송하고 응답 DTO 받기
        UpiMsgDto<MpmTrxInfo> upiResponseDto = mpqrcPymentUrl(finalJsonBody);
        log.info("MPQRC_PAYMENT_URL Response : {}", upiResponseDto);

        // 4. 서명 검증
        CommonUtil.verifyUmps(upiResponseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        // 5. QR 결제내역 업데이트
//        qrPayService.updateQrPayData(upiResponseDto);

        // 6. 은행 응답 DTO 생성 및 반환
        ApiMsgDto responseApiDto =  generateMpqrcPaymentUrlRequestBankDto(upiResponseDto);
        return ResponseEntity.ok(responseApiDto);
    }

    /**
     * MPM Back-End, Front-End 에서 사용하는 MPM-URL QR 결제 요청을 처리합니다.
     *
     * @param requestDto 은행으로부터 받은 요청 DTO
     * @return 응답 DTO
     */
    @Transactional
    public ApiMsgDto processMpqrcPaymentUrlWithEmvPayload(UpiMsgDto<MpmTrxInfo> requestDto) {
        // 0. 요청 DTO 생성
        UpiMsgDto<MpmTrxInfo> upiDto = generateMpqrcPaymentUrlRequestUpiDto(requestDto);

        // 1. To-Be-Signed String 변환 (서명할 값)
        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiDto);

        // 2. 서명 / 요청 보낼 값으로 변환
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
        log.info("MPQRC_PAYMENT_URL Request : {}", upiDto);

        // 3. UPI 요청 DTO를 API에 전송하고 응답 DTO 받기
        UpiMsgDto<MpmTrxInfo> upiResponseDto = mpqrcPymentUrl(finalJsonBody);
        log.info("MPQRC_PAYMENT_URL Response : {}",upiResponseDto);

        // 4. 서명 검증
        CommonUtil.verifyUmps(upiResponseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        // 5. QR 결제내역 업데이트
//        qrPayService.updateQrPayData(upiResponseDto);

        // 6. 은행 응답 DTO 생성 및 반환
        return generateMpqrcPaymentDto(upiResponseDto);
    }

    /**
     * 거래결과 조회 Api 요청 객체 생성
     *
     * @param apiDto 은행으로부터의 요청 데이터
     * @return 생성된 UPI 요청 DTO
     */
    public UpiMsgDto<TrxStatusNotificationTrxInfo> generateTrxResultInquiryUpiDto(ApiMsgDto apiDto) {
        TrxStatusNotificationTrxInfo trxInfo = TrxStatusNotificationTrxInfo.builder()
                .deviceID(oqUpiTokenRepository.getDeviceId(apiDto.getTrxInfo().getToken()))
                .token(apiDto.getTrxInfo().getToken())
                .txnID(apiDto.getTrxInfo().getTxnID())
                .build();

        CertificateSignature certificateSignature = CertificateSignature.builder()
                .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
                .signature("00000000")
                .build();

        return UpiMsgDto.<TrxStatusNotificationTrxInfo>builder()
                .msgInfo(CommonUtil.createMsgInfo(MessageId.A, MessageType.TRX_RESULT_INQUIRY))
                .trxInfo(trxInfo)
                .certificateSignature(certificateSignature)
                .build();
    }

    /**
     * UPI 송신 : 거래결과 조회
     *
     * @param upiDto UPI 요청 DTO
     * @return 응답 DTO
     */
    public UpiMsgDto<TrxStatusNotificationTrxInfo> trxResultInquiry(String upiDto) {
        return RestClient.create()
                .post()
                .uri(ApiSource.UNION_PAY.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(upiDto)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Api 응답 객체 생성
     *
     * @param upiDto UPI 응답 DTO
     * @return 응답 DTO
     */
    public ApiMsgDto generateTrxResultInquiryApiDto(UpiMsgDto<TrxStatusNotificationTrxInfo> upiDto) {
        SettlementKey settlementKey = null;
        if(upiDto.getTrxInfo().getSettlementKey() != null) {
             settlementKey = SettlementKey.builder()
                     .acquirerIIN(upiDto.getTrxInfo().getSettlementKey().getAcquirerIIN())
                     .forwardingIIN(upiDto.getTrxInfo().getSettlementKey().getForwardingIIN())
                     .systemTraceAuditNumber(upiDto.getTrxInfo().getSettlementKey().getSystemTraceAuditNumber())
                     .transmissionDateTime(upiDto.getTrxInfo().getSettlementKey().getTransmissionDateTime())
                     .build();
        }

        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
                .deviceID(upiDto.getTrxInfo().getDeviceID())
                .token(upiDto.getTrxInfo().getToken())
                .txnID(upiDto.getTrxInfo().getTxnID())
                .trxAmt(upiDto.getTrxInfo().getTrxAmt())
                .trxCurrency(upiDto.getTrxInfo().getTrxCurrency())
                .discountDetails(upiDto.getTrxInfo().getDiscountDetails())
                .originalAmount(upiDto.getTrxInfo().getOriginalAmount())
                .merchantName(upiDto.getTrxInfo().getMerchantName())
                .qrcVoucherNo(upiDto.getTrxInfo().getQrcVoucherNo())
                .retrievalReferenceNumber(upiDto.getTrxInfo().getRetrievalReferenceNumber())
                .settlementKey(settlementKey)
                .paymentStatus(upiDto.getTrxInfo().getPaymentStatus())
                .rejectionReason(upiDto.getTrxInfo().getRejectionReason())
                .merchantID(upiDto.getTrxInfo().getMerchantID())
                .mcc(upiDto.getTrxInfo().getMcc())
                .build();

        return ApiMsgDto.builder()
                .trxInfo(trxInfo)
                .msgResponse(upiDto.getMsgResponse())
                .build();
    }

    /**
     * Api 요청 객체 생성
     *
     * @param apiDto 은행으로부터 받은 요청 DTO
     * @return UPI로 보낼 요청 DTO
     */
    public UpiMsgDto<MpmTrxInfo> generateMpqrcPaymentUrlRequestUpiDto(ApiMsgDto apiDto) {
        OqUpiTokenDto tokenDto = oqUpiTokenRepository.getTokenByTokenOrDeviceId(apiDto.getTrxInfo().getToken());

        MsgInfo msgInfo = CommonUtil.createMsgInfo(MessageId.A, MessageType.MPQRC_PAYMENT_URL);

        RiskInfo riskInfo = RiskInfo.builder()
                .appUserID(tokenDto.getAppUserId())
                .reservedMobileNo(tokenDto.getReservedMobileNo())
                .build();

        MpmTrxInfo trxInfo = MpmTrxInfo.builder()
                .deviceID(tokenDto.getDeviceId())
                .token(apiDto.getTrxInfo().getToken())
                .txnID(apiDto.getTrxInfo().getTxnID())
                .mpqrcPayload(apiDto.getTrxInfo().getMpqrcPayload())
                .trxAmt(apiDto.getTrxInfo().getTrxAmt())
                .couponInfo(apiDto.getTrxInfo().getCouponInfo())
                .riskInfo(riskInfo)
                .build();

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
     * Api 요청 객체 생성
     *
     * @param apiDto 은행으로부터 받은 요청 DTO
     * @return UPI로 보낼 요청 DTO
     */
    public UpiMsgDto<MpmTrxInfo> generateMpqrcPaymentUrlRequestUpiDto(UpiMsgDto<MpmTrxInfo> apiDto) {
        OqUpiTokenDto tokenDto = oqUpiTokenRepository.getTokenByTokenOrDeviceId(apiDto.getTrxInfo().getToken());

        MsgInfo msgInfo = CommonUtil.createMsgInfo(MessageId.A, MessageType.MPQRC_PAYMENT_URL);

        RiskInfo riskInfo = RiskInfo.builder()
                .appUserID(tokenDto.getAppUserId())
                .reservedMobileNo(tokenDto.getReservedMobileNo())
                .build();

        MpmTrxInfo trxInfo = MpmTrxInfo.builder()
                .deviceID(tokenDto.getDeviceId())
                .token(apiDto.getTrxInfo().getToken())
                .txnID(apiDto.getTrxInfo().getTxnID())
                .mpqrcPayload(apiDto.getTrxInfo().getMpqrcPayload())
                .trxAmt(apiDto.getTrxInfo().getTrxAmt())
                .couponInfo(apiDto.getTrxInfo().getCouponInfo())
                .riskInfo(riskInfo)
                .build();

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
     *  UPI 송신 : QR 결제 요청
     *
     * @param upiDto 은행으로부터 받은 QRC 정보 조회 요청 DTO
     * @return 응답 DTO
     */
    public UpiMsgDto<MpmTrxInfo> mpqrcPymentUrl(String upiDto) {
        return RestClient.create()
                .post()
                .uri(ApiSource.UNION_PAY.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(upiDto)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Api 응답 객체 생성
     *
     * @param upiDto UPI 로부터 받은 응답 DTO
     * @return 은행으로 보낼 응답 DTO
     */
    public ApiMsgDto generateMpqrcPaymentUrlRequestBankDto(UpiMsgDto<MpmTrxInfo> upiDto) {
        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
                .token(upiDto.getTrxInfo().getToken())
                .txnID(upiDto.getTrxInfo().getTxnID())
                .trxAmt(upiDto.getTrxInfo().getTrxAmt())
                .trxCurrency(upiDto.getTrxInfo().getTrxCurrency())
                .discountDetails(upiDto.getTrxInfo().getDiscountDetails())
                .originalAmount(upiDto.getTrxInfo().getOriginalAmount())
                .merchantName(upiDto.getTrxInfo().getSubMerchantName())
                .qrcVoucherNo(upiDto.getTrxInfo().getQrcVoucherNo())
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
     * Api 응답 객체 생성
     *
     * @param upiDto UPI로부터 받은 응답 DTO
     * @return 은행으로 보낼 응답 DTO
     */
    public ApiMsgDto generateMpqrcPaymentDto(UpiMsgDto<MpmTrxInfo> upiDto) {

        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
                .token(upiDto.getTrxInfo().getToken())
                .txnID(upiDto.getTrxInfo().getTxnID())
                .trxAmt(upiDto.getTrxInfo().getTrxAmt())
                .trxCurrency(upiDto.getTrxInfo().getTrxCurrency())
                .discountDetails(upiDto.getTrxInfo().getDiscountDetails())
                .originalAmount(upiDto.getTrxInfo().getOriginalAmount())
                .merchantName(upiDto.getTrxInfo().getSubMerchantName())
                .qrcVoucherNo(upiDto.getTrxInfo().getQrcVoucherNo())
                .merchantID(upiDto.getTrxInfo().getMerchantID())
                .mcc(upiDto.getTrxInfo().getMcc())
                .settlementKey(upiDto.getTrxInfo().getSettlementKey())
                .build();

        return ApiMsgDto.builder()
                .trxInfo(trxInfo)
                .msgResponse(upiDto.getMsgResponse())
                .build();
    }
}
