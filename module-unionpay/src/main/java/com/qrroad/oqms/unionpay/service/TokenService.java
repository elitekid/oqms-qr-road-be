package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.domain.dto.OqUpiTokenDto;
import com.qrroad.oqms.domain.repository.OqUpiTokenRepository;
import com.qrroad.oqms.infrastructure.service.VaultService;
import com.qrroad.oqms.unionpay.dto.*;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.TokenTrxInfo;
import com.qrroad.oqms.unionpay.enums.ApiSource;
import com.qrroad.oqms.unionpay.enums.MessageId;
import com.qrroad.oqms.unionpay.enums.MessageType;
import com.qrroad.oqms.unionpay.keymanagement.UmpsCertificateKeyManager;
import com.qrroad.oqms.unionpay.util.CommonUtil;
import com.qrroad.oqms.unionpay.util.InstitutionApiMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@Slf4j
public class TokenService {

    private final OqUpiTokenRepository oqUpiTokenRepository;
    private final UmpsCertificateKeyManager umpsCertificateKeyManager;
    private final VaultService vaultService;

    public TokenService(
        OqUpiTokenRepository oqUpiTokenRepository,
        UmpsCertificateKeyManager umpsCertificateKeyManager,
        VaultService vaultService
    ) {
        this.oqUpiTokenRepository = oqUpiTokenRepository;
        this.umpsCertificateKeyManager = umpsCertificateKeyManager;
        this.vaultService = vaultService;
    }


    /**
     * 토큰 발급(TOKEN_REQUEST) - 은행 트랜잭션 결과를 처리
     *
     * @param apiMsgDto 은행으로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    @Transactional
    public ApiMsgDto processTokenRequest(ApiMsgDto apiMsgDto) {
        // 0. pan 번호 생성
        String pan = CommonUtil.generateCardNo();

        // 1. UPI 요청 DTO 생성
        UpiMsgDto<TokenTrxInfo> upiDto = generateTokenRequestUpiDto(apiMsgDto, pan);

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
        log.info("TOKEN_REQUEST Request : {}", finalJsonBody);

        // 4. UPI API 요청
        UpiMsgDto<TokenTrxInfo> upiResponseDto = tokenRequest(finalJsonBody);
        log.info("TOKEN_REQUEST Response : {}", upiResponseDto);

        // 5. 서명 검증
        CommonUtil.verifyUmps(upiResponseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        // 6. 토큰 정보 DB 저장
        insertToken(apiMsgDto, upiResponseDto, pan);

        // 7. 응답반환
        return generateTokenRequestApiDto(apiMsgDto, upiResponseDto, pan);
    }

    /**
     * Api 응답 객체 생성
     *
     * @param apiMsgDto api 요청 데이터
     * @param upiMsgDto UPI 응답 DTO
     * @param pan 카드 번호
     * @return 응답 DTO
     */
    public ApiMsgDto generateTokenRequestApiDto(ApiMsgDto apiMsgDto, UpiMsgDto<TokenTrxInfo> upiMsgDto, String pan) {
        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
            .deviceID(upiMsgDto.getTrxInfo().getDeviceID())
            .token(upiMsgDto.getTrxInfo().getToken())
            .pan(vaultService.encryptByDek(pan))
            .expiryDate(apiMsgDto.getTrxInfo().getExpiryDate())
            .appUserID(apiMsgDto.getTrxInfo().getAppUserID())
            .tokenState(upiMsgDto.getTrxInfo().getTokenState())
            .tokenExpiry(upiMsgDto.getTrxInfo().getTokenExpiry())
            .build();

        return ApiMsgDto.builder()
            .trxInfo(trxInfo)
            .msgResponse(upiMsgDto.getMsgResponse())
            .build();
    }
    /**
     * 토큰 업데이트(TOKEN_STATE_UPDATE) - 은행 트랜잭션 결과를 처리
     *
     * @param apiMsgDto 은행으로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    @Transactional
    public ApiMsgDto processTokenStateUpdate(ApiMsgDto apiMsgDto) {
        // 0. UPI 요청 DTO 생성
        UpiMsgDto<TokenTrxInfo> upiDto = generateTokenStateUpdateUpiDto(apiMsgDto);

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
        log.info("TOKEN_STATE_UPDATE Request : {}", finalJsonBody);

        // 3. UPI API 요청
        UpiMsgDto<TokenTrxInfo> upiResponseDto = tokenStateUpdate(finalJsonBody);
        log.info("TOKEN_STATE_UPDATE Response {}", upiResponseDto);

        // 4. 서명 검증
        CommonUtil.verifyUmps(upiResponseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        // 5. 토큰 상태 업데이트
        updateToken(apiMsgDto, upiResponseDto);

        // 6. 응답 반환
        return generateTokenStateUpdateApiDto(upiResponseDto);
        }

    /**
     * 토큰 업데이트(TOKEN_STATE_NOTIFICATION) - UPI 트랜잭션 결과를 처리
     *
     * @param upiMsgDto UPI 로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    @Transactional
    public String processTokenStateNotification(UpiMsgDto<TokenTrxInfo> upiMsgDto) {
        // 0. 서명 검증
        CommonUtil.verifyUmps(upiMsgDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        // 1. 기관 API 요청 객체 생성
        ApiMsgDto apiRequestDto = generateTokenStateNotificationApiDto(upiMsgDto);
        log.info("TOKEN_STATE_NOTIFICATION Request : {}", apiRequestDto);

        // 2. API 요청
        ApiMsgDto apiResponseDto = tokenStateNotification(apiRequestDto);
        log.info("TOKEN_STATE_NOTIFICATION Response : {}", apiResponseDto);

        // 3. 토큰 상태 업데이트
        updateToken(apiResponseDto, upiMsgDto);

        // 4. UPI 응답 객체 생성 / 반환
        return generateTokenStateNotificationUpiDto(upiMsgDto, apiResponseDto);
        }

    /**
     * 토큰 발급(TOKEN_REQUEST) - UPI API 통신
     *
     * @param upiDto 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    private UpiMsgDto<TokenTrxInfo> tokenRequest(String upiDto) {
        return RestClient.create()
            .post()
            .uri(ApiSource.UNION_PAY.getUrl())
            .contentType(MediaType.APPLICATION_JSON)
            .body(upiDto)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * 토큰 업데이트(TOKEN_STATE_UPDATE) - 은행 -> UPI API통신
     *
     * @param upiDto 은행으로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    private UpiMsgDto<TokenTrxInfo> tokenStateUpdate(String upiDto) {
        return RestClient.create()
            .post()
            .uri(ApiSource.UNION_PAY.getUrl())
            .contentType(MediaType.APPLICATION_JSON)
            .body(upiDto)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * 토큰 업데이트(TOKEN_STATE_NOTIFICATION) - UPI -> 은행 API통신
     *
     * @param apiMsgDto UPI 로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    private ApiMsgDto tokenStateNotification(ApiMsgDto apiMsgDto) {
        return RestClient.create()
            .post()
            .uri(InstitutionApiMapper.getApiUrlByInstitutionCode(
                oqUpiTokenRepository.getInstCd(apiMsgDto.getTrxInfo().getToken())
            ))
            .contentType(MediaType.APPLICATION_JSON)
            .body(apiMsgDto)
            .retrieve()
            .body(ApiMsgDto.class);
    }

    /**
     * 토큰 발급(TOKEN_REQUEST) - 데이터 가공
     *
     * @param apiMsgDto api 요청 데이터
     * @param pan 카드 번호
     * @return 처리된 결과를 리턴합니다.
     */
    public UpiMsgDto<TokenTrxInfo> generateTokenRequestUpiDto(ApiMsgDto apiMsgDto, String pan) {
        try {
            MsgInfo msgInfo = CommonUtil.createMsgInfo(MessageId.U, MessageType.TOKEN_REQUEST);

            LocalDate expirySetting = LocalDate.now().plusYears(5);
            String expiryDate = expirySetting.format(DateTimeFormatter.ofPattern("MM/yy"));

            RiskInfo riskInfo = RiskInfo.builder()
                .reservedMobileNo("82-1000000000")
                .build();

            TokenTrxInfo trxInfo = TokenTrxInfo.builder()
                .deviceID(apiMsgDto.getTrxInfo().getDeviceID())
                .pan(CommonUtil.encryptByPublicKey(
                    pan.getBytes(),
                    umpsCertificateKeyManager.getUmpsKeys().getUmpsEncPublicKey()
                ))
                .useCaseIndicator(new String[]{"QRC"})
                .expiryDate(expiryDate)
                .cvm(new String[]{"expireDate"})
                .riskInfo(riskInfo)
                .build();

            CertificateSignature certificateSignature = CertificateSignature.builder()
                .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
                .umpsEncCertID(umpsCertificateKeyManager.getUmpsKeys().getUmpsEncCertId())
                .signature("00000000")
                .build();

            return UpiMsgDto.<TokenTrxInfo>builder()
                .msgInfo(msgInfo)
                .trxInfo(trxInfo)
                .certificateSignature(certificateSignature)
                .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 토큰 업데이트(TOKEN_STATE_UPDATE) - 데이터 가공
     *
     * @param dto 은행로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    public UpiMsgDto<TokenTrxInfo> generateTokenStateUpdateUpiDto(ApiMsgDto dto) {
        MsgInfo msgInfo = CommonUtil.createMsgInfo(MessageId.A, MessageType.TOKEN_STATE_UPDATE);

        String deviceID = oqUpiTokenRepository.getDeviceId(dto.getTrxInfo().getToken());

        TokenTrxInfo trxInfo = TokenTrxInfo.builder()
            .deviceID(deviceID)
            .token(dto.getTrxInfo().getToken())
            .tokenAction(dto.getTrxInfo().getTokenAction())
            .build();

        CertificateSignature certificateSignature = CertificateSignature.builder()
            .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
            .signature("00000000")
            .build();

        return UpiMsgDto.<TokenTrxInfo>builder()
            .msgInfo(msgInfo)
            .trxInfo(trxInfo)
            .certificateSignature(certificateSignature)
            .build();
        }

    /**
     * 토큰 업데이트(TOKEN_STATE_NOTIFICATION) - 데이터 가공
     *
     * @param dto UPI로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    public ApiMsgDto generateTokenStateNotificationApiDto(UpiMsgDto<TokenTrxInfo> dto) {
        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
            .token(dto.getTrxInfo().getToken())
            .tokenState(dto.getTrxInfo().getTokenState())
            .build();

        return ApiMsgDto.builder()
            .trxInfo(trxInfo)
            .build();
        }

    /**
     * Api 응답 객체 생성
     *
     * @param upiMsgDto UPI 응답 DTO
     * @return 응답 DTO
     */
    public ApiMsgDto generateTokenStateUpdateApiDto(UpiMsgDto<TokenTrxInfo> upiMsgDto) {
        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
            .token(upiMsgDto.getTrxInfo().getToken())
            .tokenState(upiMsgDto.getTrxInfo().getTokenState())
            .build();

        return ApiMsgDto.builder()
            .trxInfo(trxInfo)
            .msgResponse(upiMsgDto.getMsgResponse())
            .build();
    }

    /**
     * Api 응답 객체 생성
     * @param upiMsgDto UPI 응답 DTO
     * @param apiMsgDto API 응답 DTO
     * @return 응답 DTO
     */
    public String generateTokenStateNotificationUpiDto(UpiMsgDto<TokenTrxInfo> upiMsgDto, ApiMsgDto apiMsgDto) {
        CertificateSignature certificateSignature = CertificateSignature.builder()
            .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
            .signature("00000000")
            .build();

        upiMsgDto = upiMsgDto.toBuilder()
            .msgInfo(upiMsgDto.getMsgInfo())
            .trxInfo(TokenTrxInfo.builder().build())
            .certificateSignature(certificateSignature)
            .msgResponse(apiMsgDto.getMsgResponse())
            .build();

        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiMsgDto);

        upiMsgDto = upiMsgDto.toBuilder()
            .certificateSignature(
                upiMsgDto.getCertificateSignature()
                    .toBuilder()
                    .signature(CommonUtil.signUmps(
                        Objects.requireNonNull(jsonBody).getBytes(),
                        umpsCertificateKeyManager.getPrivateKeys().getAppPrivateSignature2048()
                    ))
                    .build()
            )
            .build();

        return CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiMsgDto);
    }

    /**
     * 토큰 정보 DB 저장
     *
     * @param upiMsgDto UPI 응답 DTO
     */
    public void insertToken(ApiMsgDto apiMsgDto, UpiMsgDto<TokenTrxInfo> upiMsgDto, String pan) {
        OqUpiTokenDto tokenDto = OqUpiTokenDto.builder()
            .instCd(apiMsgDto.getPayInfo().getInstCd())
            .deviceId(apiMsgDto.getTrxInfo().getDeviceID())
            .token(upiMsgDto.getTrxInfo().getToken())
            .tokenRefId(upiMsgDto.getTrxInfo().getTokenRefID())
            .tokenState(upiMsgDto.getTrxInfo().getTokenState())
            .tokenExpiry(upiMsgDto.getTrxInfo().getTokenExpiry())
            .pan(vaultService.encryptByDek(pan))
            .appUserId(apiMsgDto.getTrxInfo().getAppUserID())
            .reservedMobileNo("82-1000000000")
            .build();

        oqUpiTokenRepository.save(tokenDto);
    }

    /**
     * 토큰 정보 DB 업데이트
     *
     * @param upiMsgDto UPI 응답 DTO
     */
    public void updateToken(ApiMsgDto apiMsgDto, UpiMsgDto<TokenTrxInfo> upiMsgDto) {
        OqUpiTokenDto tokenDto = OqUpiTokenDto.builder()
            .instCd(apiMsgDto.getPayInfo().getInstCd())
            .deviceId(apiMsgDto.getTrxInfo().getDeviceID())
            .token(upiMsgDto.getTrxInfo().getToken())
            .tokenState(upiMsgDto.getTrxInfo().getTokenState())
            .build();

        oqUpiTokenRepository.save(tokenDto);
    }
}
