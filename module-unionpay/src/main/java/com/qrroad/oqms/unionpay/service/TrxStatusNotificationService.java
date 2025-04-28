package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.domain.repository.OqUpiTokenRepository;
import com.qrroad.oqms.unionpay.dto.ApiMsgDto;
import com.qrroad.oqms.unionpay.dto.CertificateSignature;
import com.qrroad.oqms.unionpay.dto.UpiMsgDto;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.TrxStatusNotificationTrxInfo;
import com.qrroad.oqms.unionpay.enums.ApiSource;
import com.qrroad.oqms.unionpay.keymanagement.UmpsCertificateKeyManager;
import com.qrroad.oqms.unionpay.util.CommonUtil;
import com.qrroad.oqms.unionpay.util.InstitutionApiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrxStatusNotificationService {
    private final OqUpiTokenRepository oqUpiTokenRepository;
    private final UmpsCertificateKeyManager umpsCertificateKeyManager;

    /**
     * 토큰 업데이트(TOKEN_STATE_NOTIFICATION) - UPI 트랜잭션 결과를 처리
     *
     * @param upiMsgDto UPI 로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    public String processTrxStatusNotification(UpiMsgDto<TrxStatusNotificationTrxInfo> upiMsgDto) {
        // 0. 서명 검증
        CommonUtil.verifyUmps(upiMsgDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        ApiMsgDto apiMsgDto = generateTrxStatusNotificationApiDto(upiMsgDto);

        ApiMsgDto responseApiDto = trxStatusNotification(apiMsgDto);

        // 1. 응답 DTO 생성
        UpiMsgDto<TrxStatusNotificationTrxInfo> responseUpiDto = generateTrxStatusNotificationUpiDto(responseApiDto, upiMsgDto);

        // 2. To-Be-Signed String 변환 (서명할 값)
        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(responseUpiDto);

        // 3. 서명 / 응답 보낼 값으로 변환
        responseUpiDto = responseUpiDto.toBuilder()
                .certificateSignature(
                        responseUpiDto.getCertificateSignature()
                                .toBuilder()
                                .signature(CommonUtil.signUmps(
                                        Objects.requireNonNull(jsonBody).getBytes(),
                                        umpsCertificateKeyManager.getPrivateKeys().getAppPrivateSignature2048()
                                ))
                                .build()
                )
                .build();
        String finalJsonBody =  CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(responseUpiDto);
        log.info("GET_USER_ID response : {}", responseUpiDto);

        return finalJsonBody;

    }

    /**
     * 거래상태 확인 요청(TRX_STATUS_NOTIFICATION) - UPI -> 은행 API 통신
     *
     * @param apiMsgDto UPI 로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    private ApiMsgDto trxStatusNotification(ApiMsgDto apiMsgDto) {
        return RestClient.create()
                .post()
                .uri(InstitutionApiMapper.getApiUrlByInstitutionCode(oqUpiTokenRepository.getInstCd(apiMsgDto.getTrxInfo().getToken())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(apiMsgDto)
                .retrieve()
                .body(ApiMsgDto.class);
    }

    /**
     * 거래상태 확인 요청(TRX_STATUS_NOTIFICATION) - 데이터 가공
     *
     * @param upiMsgDto UPI 로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    public ApiMsgDto generateTrxStatusNotificationApiDto(UpiMsgDto<TrxStatusNotificationTrxInfo> upiMsgDto) {
        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
                .token(upiMsgDto.getTrxInfo().getToken())
                .txnID(upiMsgDto.getTrxInfo().getTxnID())
                .emvCpqrcPayload(upiMsgDto.getTrxInfo().getEmvCpqrcPayload())
                .barcodeCpqrcPayload(upiMsgDto.getTrxInfo().getBarcodeCpqrcPayload())
                .trxAmt(upiMsgDto.getTrxInfo().getTrxAmt())
                .trxCurrency(upiMsgDto.getTrxInfo().getTrxCurrency())
                .discountDetails(upiMsgDto.getTrxInfo().getDiscountDetails())
                .originalAmount(upiMsgDto.getTrxInfo().getOriginalAmount())
                .merchantName(upiMsgDto.getTrxInfo().getMerchantName())
                .qrcVoucherNo(upiMsgDto.getTrxInfo().getQrcVoucherNo())
                .merchantID(upiMsgDto.getTrxInfo().getMerchantID())
                .mcc(upiMsgDto.getTrxInfo().getMcc())
                .settlementKey(upiMsgDto.getTrxInfo().getSettlementKey())
                .paymentStatus(upiMsgDto.getTrxInfo().getPaymentStatus())
                .rejectionReason(upiMsgDto.getTrxInfo().getRejectionReason())
                .build();

        return ApiMsgDto.builder()
                .trxInfo(trxInfo)
                .msgResponse(upiMsgDto.getMsgResponse())
                .build();
    }

    /**
     * 거래상태 확인 요청(TRX_STATUS_NOTIFICATION) - 데이터 가공
     *
     * @param apiMsgDto UPI 로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    public UpiMsgDto<TrxStatusNotificationTrxInfo> generateTrxStatusNotificationUpiDto(ApiMsgDto apiMsgDto, UpiMsgDto<TrxStatusNotificationTrxInfo> upiMsgDto) {
        TrxStatusNotificationTrxInfo trxInfo = TrxStatusNotificationTrxInfo.builder()
                .token(upiMsgDto.getTrxInfo().getToken())
                .build();

        CertificateSignature certificateSignature = CertificateSignature.builder()
                .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
                .signature("00000000")
                .build();

        return UpiMsgDto.<TrxStatusNotificationTrxInfo>builder()
                .msgInfo(upiMsgDto.getMsgInfo())
                .trxInfo(trxInfo)
                .certificateSignature(certificateSignature)
                .msgResponse(apiMsgDto.getMsgResponse())
                .build();
    }
}
