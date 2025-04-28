package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.domain.dto.OqUpiTokenDto;
import com.qrroad.oqms.domain.repository.OqUpiTokenRepository;
import com.qrroad.oqms.unionpay.dto.*;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.CpqrcGenerationTrxInfo;
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
import org.springframework.web.client.RestClient;

import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class CPMService {
    private final UmpsCertificateKeyManager umpsCertificateKeyManager;
    private final QrPayService qrPayService;
    private final OqUpiTokenRepository oqUpiTokenRepository;

    /**
     * QR생성 요청(CPQRC_GENERATION) - 은행 트랜잭션 결과를 처리
     *
     * @param dto 은행으로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    public ApiMsgDto processCpqrcGeneration(ApiMsgDto dto) {
        UpiMsgDto<CpqrcGenerationTrxInfo> upiDto = generateCpqrcGenerationUpiDto(dto);

//        qrPayService.createQrPayData(cpqrcGenerationUpiDto,dto.getTrxInfo().getTrxCl());

        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiDto);

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
        log.info("CPQRC_GENERATION Request : {}", upiDto);

        UpiMsgDto<CpqrcGenerationTrxInfo> responseDto = cpqrcGeneration(finalJsonBody);
        log.info("CPQRC_GENERATION Response : {}", responseDto);

        CommonUtil.verifyUmps(responseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        return generateApiDto(responseDto);
    }

    /**
     * QR생성 요청(CPQRC_GENERATION) - 데이터 가공
     *
     * @param dto 은행으로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    public UpiMsgDto<CpqrcGenerationTrxInfo> generateCpqrcGenerationUpiDto(ApiMsgDto dto) {
        OqUpiTokenDto oqUpiTokenDto = oqUpiTokenRepository.getTokenByTokenOrDeviceId(dto.getTrxInfo().getToken());

        MsgInfo msgInfo = CommonUtil.createMsgInfo(MessageId.A, MessageType.CPQRC_GENERATION);

        RiskInfo riskInfo = RiskInfo.builder()
                .appUserID(oqUpiTokenDto.getAppUserId())
                .reservedMobileNo(oqUpiTokenDto.getReservedMobileNo())
                .build();

        CpqrcGenerationTrxInfo trxInfo = CpqrcGenerationTrxInfo.builder()
                .token(dto.getTrxInfo().getToken())
                .deviceID(oqUpiTokenDto.getDeviceId())
                .cpqrcNo("1")
                .couponInfo(dto.getTrxInfo().getCouponInfo())
                .riskInfo(riskInfo)
                .build();

        CertificateSignature certificateSignature = CertificateSignature.builder()
                .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
                .umpsEncCertID(umpsCertificateKeyManager.getUmpsKeys().getUmpsEncCertId())
                .signature("00000000")
                .build();

        return UpiMsgDto.<CpqrcGenerationTrxInfo>builder()
                .msgInfo(msgInfo)
                .trxInfo(trxInfo)
                .certificateSignature(certificateSignature)
                .build();
    }

    public UpiMsgDto<CpqrcGenerationTrxInfo> cpqrcGeneration(String finalJsonBody) {
        return RestClient.create()
                .post()
                .uri(ApiSource.UNION_PAY.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(finalJsonBody)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Api 응답 객체 생성
     *
     * @param upiDto UPI 응답 DTO
     * @return 응답 DTO
     */
    public ApiMsgDto generateApiDto(UpiMsgDto<CpqrcGenerationTrxInfo> upiDto) {
        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
                .emvCpqrcPayload(upiDto.getTrxInfo().getEmvCpqrcPayload())
                .barcodeCpqrcPayload(upiDto.getTrxInfo().getBarcodeCpqrcPayload())
                .build();

        return ApiMsgDto.builder()
                .trxInfo(trxInfo)
                .msgResponse(upiDto.getMsgResponse())
                .build();
    }

}
