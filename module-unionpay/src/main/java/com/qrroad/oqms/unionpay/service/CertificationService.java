package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.infrastructure.service.VaultService;
import com.qrroad.oqms.unionpay.dto.CertificateSignature;
import com.qrroad.oqms.unionpay.dto.MsgInfo;
import com.qrroad.oqms.unionpay.dto.UpiMsgDto;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.KeyTrxInfo;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificationService {
    private final VaultService vaultService;
    private final UmpsCertificateKeyManager umpsCertificateKeyManager;

    // 하루에 한 번 수행 : SchedulerConfig 에서 처리
    // App 인증서 재발급 했을 때는 API 로 관리자가 직접 실행
    public void processKeyInquiryExchange() {
        // 0. UPI 요청 객체 생성
        UpiMsgDto<KeyTrxInfo> upiDto = generateKeyInquiryExchange();

        // 1. To-Be-Signed String 변환 (서명할 값)
        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiDto);
        log.info("KEY_INQUIRY_EXCHANGE To-Be-Signed String : {}", jsonBody);

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
        log.info("KEY_INQUIRY_EXCHANGE Request : {}", finalJsonBody);

        // 3. HTTP 요청 전송
        UpiMsgDto<KeyTrxInfo> upiResponseDto = keyInquiryExchange(finalJsonBody);
        log.info("KEY_INQUIRY_EXCHANGE Response : {}", upiResponseDto);

        // 4. 서명 검증
        CommonUtil.verifyUmps(upiResponseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        // 5. Vault 업데이트
        if (upiResponseDto.getMsgResponse().getResponseCode().equals("00")) {
            updateVaultAppAndPrivateData();
        }

        // 5. 응답 후처리 이벤트 발행 (umpsSignCertID, umpsEncCertID 둘 중 하나가 응답 값에 포함되어 있을 경우)
        if(!upiResponseDto.getTrxInfo().getUmpsSignCertID().isEmpty() || !upiResponseDto.getTrxInfo().getUmpsEncCertID().isEmpty()) {
            processKeyResetResult(upiResponseDto);
        }
    }

    public void processKeyResetResult(UpiMsgDto<KeyTrxInfo> upiMsgDto) {
        // 0. UPI 요청 객체 생성
        UpiMsgDto<KeyTrxInfo> upiDto = generateKeyResetResultUpiDto(upiMsgDto);

        // 1. To-Be-Signed String 변환 (서명할 값)
        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiDto);
        log.info("KEY_RESET_RESULT To-Be-Signed String : {}", jsonBody);

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
        log.info("KEY_RESET_RESULT Request : {}", finalJsonBody);

        // 3. HTTP 요청 전송
        UpiMsgDto<KeyTrxInfo> responseDto = keyResetResult(finalJsonBody);
        log.info("KEY_RESET_RESULT Response : {}", responseDto);

        // 4. 서명 검증
        CommonUtil.verifyUmps(responseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

        // 5. Vault 업데이트
        updateVaultUmpsData(upiMsgDto);
    }

    private UpiMsgDto<KeyTrxInfo> generateKeyInquiryExchange() {
        Map<String, Object> newCertData = vaultService.getSecretData("/umps_certificate/data/new_app");
        String appSignCertId = (String) newCertData.getOrDefault("appSignCertId", null);
        String appSignPublicKey = (String) newCertData.getOrDefault("appSignPublicKey", null);
        String appEncCertId = (String) newCertData.getOrDefault("appEncCertId", null);
        String appEncPublicKey = (String) newCertData.getOrDefault("appEncPublicKey", null);

        Map<String, Object> umpsData = vaultService.getSecretData("/umps_certificate/data/new_app");
        String umpsSignCertId = (String) umpsData.getOrDefault("umpsSignCertId", null);
        String umpsEncCertId = (String) umpsData.getOrDefault("umpsEncCertId", null);

        MsgInfo msgInfo = CommonUtil.createMsgInfo(MessageId.A, MessageType.KEY_INQUIRY_EXCHANGE);

        KeyTrxInfo trxInfo = KeyTrxInfo.builder()
                .appSignCertID(appSignCertId)
                .appSignPublicKey(appSignPublicKey)
                .appEncCertID(appEncCertId)
                .appEncPublicKey(appEncPublicKey)
                .umpsSignCertID(umpsSignCertId)
                .umpsEncCertID(umpsEncCertId)
                .build();

        CertificateSignature certificateSignature = CertificateSignature.builder()
                .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
                .signature("00000000")
                .build();

        return UpiMsgDto.<KeyTrxInfo>builder()
                .msgInfo(msgInfo)
                .trxInfo(trxInfo)
                .certificateSignature(certificateSignature)
                .build();
    }

    private UpiMsgDto<KeyTrxInfo> generateKeyResetResultUpiDto(UpiMsgDto<KeyTrxInfo> upiMsgDto) {
        MsgInfo msgInfo = CommonUtil.createMsgInfo(MessageId.A, MessageType.KEY_RESET_RESULT);

        KeyTrxInfo trxInfo = KeyTrxInfo.builder()
                .umpsSignCertID(upiMsgDto.getTrxInfo().getUmpsSignCertID())
                .umpsEncCertID(upiMsgDto.getTrxInfo().getUmpsEncCertID())
                .build();

        CertificateSignature certificateSignature = CertificateSignature.builder()
                .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
                .signature("00000000")
                .build();

        return UpiMsgDto.<KeyTrxInfo>builder()
                .msgInfo(msgInfo)
                .trxInfo(trxInfo)
                .certificateSignature(certificateSignature)
                .build();
    }

    private void updateVaultAppAndPrivateData() {
        Map<String, Object> newCertAppData = vaultService.getSecretData("/umps_certificate/data/new_app");
        Map<String, Object> newCertPrivateData = vaultService.getSecretData("/umps_certificate/data/new_private");

        Map<String, Object> newUpdatedAppData = new HashMap<>();
        Map<String, Object> newUpdatedPrivateData = new HashMap<>();

        if(newCertAppData.get("appSignCertId") != null) {
            newUpdatedAppData.put("appSignCertId", newCertAppData.get("appSignCertId"));
            newUpdatedAppData.put("appSignPublicKey", newCertAppData.get("appSignPublicKey"));
            newUpdatedPrivateData.put("appPublicSignature2048", newCertPrivateData.get("appPublicSignature2048"));
            newUpdatedPrivateData.put("appPrivateSignature2048", newCertPrivateData.get("appPrivateSignature2048"));
        }
        if(newCertAppData.get("appEncCertId") != null) {
            newUpdatedAppData.put("appEncCertId", newCertAppData.get("appEncCertId"));
            newUpdatedAppData.put("appEncPublicKey", newCertAppData.get("appEncPublicKey"));
            newUpdatedPrivateData.put("appPublicEncrypt2048", newCertPrivateData.get("appPublicEncrypt2048"));
            newUpdatedPrivateData.put("appPrivateEncrypt2048", newCertPrivateData.get("appPrivateEncrypt2048"));
        }

        vaultService.mergeAndUpdateKvValue("/umps_certificate/data/app", newUpdatedAppData);
        vaultService.mergeAndUpdateKvValue("/umps_certificate/data/private", newUpdatedPrivateData);

        Map<String, Object> clearedNewApp = new HashMap<>();
        clearedNewApp.put("appSignCertId", null);
        clearedNewApp.put("appSignPublicKey", null);
        clearedNewApp.put("appEncCertId", null);
        clearedNewApp.put("appEncPublicKey", null);

        Map<String, Object> clearedNewPrivate = new HashMap<>();
        clearedNewApp.put("appPrivateEncrypt2048", null);
        clearedNewApp.put("appPrivateSignature2048", null);
        clearedNewApp.put("appPublicEncrypt2048", null);
        clearedNewApp.put("appPublicSignature2048", null);

        vaultService.mergeAndUpdateKvValue("/umps_certificate/data/new_app", clearedNewApp);
        vaultService.mergeAndUpdateKvValue("/umps_certificate/data/new_private", clearedNewPrivate);
    }

    private void updateVaultUmpsData(UpiMsgDto<KeyTrxInfo> upiMsgDto) {
        Map<String, Object> newUpdatedUmpsData = new HashMap<>();

        if(upiMsgDto.getTrxInfo().getUmpsSignCertID() != null) {
            newUpdatedUmpsData.put("umpsSignCertId", upiMsgDto.getTrxInfo().getUmpsSignCertID());
            newUpdatedUmpsData.put("umpsSignPublicKey", upiMsgDto.getTrxInfo().getUmpsSignPublicKey());
        }

        if(upiMsgDto.getTrxInfo().getUmpsEncCertID() != null) {
            newUpdatedUmpsData.put("umpsEncCertId", upiMsgDto.getTrxInfo().getAppEncCertID());
            newUpdatedUmpsData.put("umpsEncPublicKey", upiMsgDto.getTrxInfo().getAppEncPublicKey());
        }

        vaultService.mergeAndUpdateKvValue("/umps_certificate/data/umps", newUpdatedUmpsData);
    }

    private UpiMsgDto<KeyTrxInfo> keyInquiryExchange(String upiDto) {
        return RestClient.create()
                .post()
                .uri(ApiSource.UNION_PAY.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(upiDto)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private UpiMsgDto<KeyTrxInfo> keyResetResult(String upiDto) {
        return RestClient.create()
                .post()
                .uri(ApiSource.UNION_PAY.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(upiDto)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}


