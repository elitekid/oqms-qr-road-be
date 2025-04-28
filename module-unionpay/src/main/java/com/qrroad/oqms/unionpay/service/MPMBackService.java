package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.domain.repository.OqUpiTokenRepository;
import com.qrroad.oqms.unionpay.dto.ApiMsgDto;
import com.qrroad.oqms.unionpay.dto.CertificateSignature;
import com.qrroad.oqms.unionpay.dto.MsgInfo;
import com.qrroad.oqms.unionpay.dto.UpiMsgDto;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.QrcInfoInquiryTrxInfo;
import com.qrroad.oqms.unionpay.enums.ApiSource;
import com.qrroad.oqms.unionpay.enums.MessageId;
import com.qrroad.oqms.unionpay.enums.MessageType;
import com.qrroad.oqms.unionpay.keymanagement.UmpsCertificateKeyManager;
import com.qrroad.oqms.unionpay.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MPMBackService {

    private final QrPayService qrPayService;
    private final OqUpiTokenRepository oqUpiTokenRepository;
    private final UmpsCertificateKeyManager umpsCertificateKeyManager;

    /**
     * QRC 정보 조회 요청을 처리합니다.
     *
     * @param apiMsgDto 은행에서 받은 QRC 정보 조회 요청 DTO
     * @return 은행으로의 응답 DTO
     */
    @Transactional
    public ApiMsgDto processQrcInfoInquiry(ApiMsgDto apiMsgDto) {
        // 0. 요청 DTO 생성
        UpiMsgDto<QrcInfoInquiryTrxInfo> upiDto = generateQrcInfoInquiryRequestUpiDto(apiMsgDto);

        // 1. QR 결제내역 생성
//        qrPayService.createQrPayData(upiRequestDto, requestDto.getPayInfo());

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
        log.info("QRC_INFO_INQUIRY request : {}", upiDto);

        // 4. UPI Api 호출 및 응답 수신
        UpiMsgDto<QrcInfoInquiryTrxInfo> upiResponseDto = qrcInfoInquiry(finalJsonBody);
        log.info("QRC_INFO_INQUIRY response : {}", upiResponseDto);

        // 5. 서명 검증
        CommonUtil.verifyUmps(upiResponseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        // 6. mcc 가 6011인 경우 인출거래용 QR 코드 이기 때문에 에러 반환
        if(Objects.equals(upiResponseDto.getTrxInfo().getMcc(), "6011")) {
//            throw new CustomException(INVALID_TRANSACTION.getCode(), INVALID_TRANSACTION.getMessage());
        }

        // 7. QR 결제내역 업데이트 (결제 완료 X)
//        qrPayService.updateQrPayData(upiResponseDto);

        // 8. 은행 응답 DTO 생성 및 반환
        return generateQrcInfoInquiryRequestApiDto(upiResponseDto, apiMsgDto.getTrxInfo().getToken());
    }

    /**
     * Api 요청 객체 생성
     *
     * @param apiMsgDto 은행으로부터 받은 요청 DTO
     * @return UPI로 보낼 요청 DTO
     */
    public UpiMsgDto<QrcInfoInquiryTrxInfo> generateQrcInfoInquiryRequestUpiDto (ApiMsgDto apiMsgDto) {
        MsgInfo msgInfo = CommonUtil.createMsgInfo(MessageId.A, MessageType.QRC_INFO_INQUIRY);

        QrcInfoInquiryTrxInfo trxInfo = QrcInfoInquiryTrxInfo.builder()
                .deviceID(oqUpiTokenRepository.getDeviceId(apiMsgDto.getTrxInfo().getToken()))
                .txnID(apiMsgDto.getTrxInfo().getTxnID() != null ? apiMsgDto.getTrxInfo().getTxnID() : CommonUtil.generateTnxID())
                .mpqrcPayload(apiMsgDto.getTrxInfo().getMpqrcPayload())
                .build();

        CertificateSignature certificateSignature = CertificateSignature.builder()
                .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
                .signature("00000000")
                .build();

        return UpiMsgDto.<QrcInfoInquiryTrxInfo>builder()
                .msgInfo(msgInfo)
                .trxInfo(trxInfo)
                .certificateSignature(certificateSignature)
                .build();
    }

    /**
     * UPI 송신 : QRC 정보 조회 요청
     *
     * @param upiDto UPI 요청 DTO
     * @return 응답 DTO
     */
    private UpiMsgDto<QrcInfoInquiryTrxInfo> qrcInfoInquiry(String upiDto) {
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
     * @param upiMsgDto UPI 응답 DTO
     * @return 응답 DTO
     */
    public ApiMsgDto generateQrcInfoInquiryRequestApiDto (UpiMsgDto<QrcInfoInquiryTrxInfo> upiMsgDto, String token) {
        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
                .token(token)
                .deviceID(upiMsgDto.getTrxInfo().getDeviceID())
                .mpqrcPayload(upiMsgDto.getTrxInfo().getMpqrcPayload())
                .txnID(upiMsgDto.getTrxInfo().getTxnID())
                .trxAmt(upiMsgDto.getTrxInfo().getTrxAmt())
                .trxCurrency(upiMsgDto.getTrxInfo().getTrxCurrency())
                .merchantName(upiMsgDto.getTrxInfo().getMerchantName())
                .mcc(upiMsgDto.getTrxInfo().getMcc())
                .build();

        return ApiMsgDto.builder()
                .trxInfo(trxInfo)
                .msgResponse(upiMsgDto.getMsgResponse())
                .build();
    }
}