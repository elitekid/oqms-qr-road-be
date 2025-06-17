package com.qrroad.oqms.payment.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qrroad.oqms.domain.dto.OqUserTokenDto;
import com.qrroad.oqms.domain.repository.OqUserTokenRepository;
import com.qrroad.oqms.payment.gateway.dto.MsgDto;
import com.qrroad.oqms.payment.gateway.dto.MsgResponse;
import com.qrroad.oqms.payment.gateway.dto.trxinfo.UserTokenTrxInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserTokenService {
    private final OqUserTokenRepository oqUserTokenRepository;
    private static final String FIXED_PREFIX = "629269";
    private static final int TOTAL_LENGTH = 16;

    /**
     * 토큰 발급(TOKEN_REQUEST) - 은행 트랜잭션 결과를 처리
     *
     * @param userTokenDto 은행으로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    @Transactional
    public ResponseEntity<?> processTokenRequest(MsgDto<UserTokenTrxInfo> userTokenDto) {
        OqUserTokenDto userToken = oqUserTokenRepository.getUserToken(userTokenDto.getTrxInfo().getUserToken() != null ? userTokenDto.getTrxInfo().getUserToken() : "");

        if(userToken != null) {
            MsgResponse msgResponse = MsgResponse.builder()
                .responseCode("99")
                .responseMsg("토큰이 이미 존재합니다.")
                .build();
            return ResponseEntity.ok(msgResponse);
        }

        LocalDate expirySetting = LocalDate.now().plusYears(5);
        String expiryDate = expirySetting.format(DateTimeFormatter.ofPattern("MM/yy"));
        String token = generateToken();

        OqUserTokenDto userTokenSaveDto = OqUserTokenDto.builder()
            .userToken(token)
            .instCd(userTokenDto.getPayInfo().getInstCd())
            .deviceId(userTokenDto.getTrxInfo().getDeviceId())
            .appUserId(userTokenDto.getTrxInfo().getAppUserId())
            .tokenState("00")
            .tokenExpiry(expiryDate)
            .build();

        oqUserTokenRepository.save(userTokenSaveDto);

        MsgResponse msgResponse = MsgResponse.builder()
            .responseCode("00")
            .responseMsg("Approved")
            .build();

        return ResponseEntity.ok(
            userTokenDto.toBuilder()
                .trxInfo(
                    userTokenDto.getTrxInfo().toBuilder()
                        .userToken(token)
                        .tokenState("00")
                        .tokenExpiry(expiryDate)
                        .build()
                )
                .msgResponse(msgResponse)
                .build()
            );
    }

    /**
     * 토큰 업데이트(TOKEN_STATE_UPDATE) - 은행 트랜잭션 결과를 처리
     *
     * @param userTokenDto 은행으로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    @Transactional
    public ResponseEntity<?> processTokenStateUpdate(MsgDto<UserTokenTrxInfo> userTokenDto) {
        OqUserTokenDto userToken = oqUserTokenRepository.getUserToken(userTokenDto.getTrxInfo().getUserToken());

        if(userToken == null) {
            MsgResponse msgResponse = MsgResponse.builder()
                .responseCode("99")
                .responseMsg("토큰이 존재하지 않습니다. 토큰 생성을 먼저 진행하세요.")
                .build();
            return ResponseEntity.ok(msgResponse);
        }

        OqUserTokenDto userTokenSaveDto = OqUserTokenDto.builder()
            .userToken(userTokenDto.getTrxInfo().getUserToken())
            .tokenState(userTokenDto.getTrxInfo().getTokenAction())
            .build();

        oqUserTokenRepository.save(userTokenSaveDto);

        return ResponseEntity.ok(
            userTokenDto.toBuilder()
                .trxInfo(
                    userTokenDto.getTrxInfo().toBuilder()
                    .tokenState(userTokenDto.getTrxInfo().getTokenAction())
                    .build()
                ).msgResponse(
                    MsgResponse.builder()
                    .responseCode("00")
                    .responseMsg("Approved")
                    .build()
                ).build()
        );
    }

    /**
     * 토큰 업데이트(TOKEN_STATE_UPDATE) - 은행 트랜잭션 결과를 처리
     *
     * @param userTokenDto 은행으로부터의 요청 데이터
     * @return 처리된 결과를 리턴합니다.
     */
    @Transactional
    public ResponseEntity<?> processTokenExpiryUpdate(MsgDto<UserTokenTrxInfo> userTokenDto) {
        OqUserTokenDto userToken = oqUserTokenRepository.getUserToken(userTokenDto.getTrxInfo().getUserToken());

        if(userToken == null) {
            MsgResponse msgResponse = MsgResponse.builder()
                .responseCode("99")
                .responseMsg("토큰이 존재하지 않습니다. 토큰 생성을 먼저 진행하세요.")
                .build();
            return ResponseEntity.ok(msgResponse);
        }

        LocalDate expirySetting = LocalDate.now().plusYears(5);
        String expiryDate = expirySetting.format(DateTimeFormatter.ofPattern("MM/yy"));

        OqUserTokenDto userTokenSaveDto = OqUserTokenDto.builder()
            .userToken(userTokenDto.getTrxInfo().getUserToken())
            .tokenExpiry(expiryDate)
            .build();

        oqUserTokenRepository.save(userTokenSaveDto);

        return ResponseEntity.ok(
            userTokenDto.toBuilder()
                .trxInfo(
                    userTokenDto.getTrxInfo().toBuilder()
                    .tokenExpiry(expiryDate)
                    .build()
                ).msgResponse(
                    MsgResponse.builder()
                    .responseCode("00")
                    .responseMsg("Approved")
                    .build()
                ).build()
        );
    }

    public static String generateToken() {
        Random random = new Random();
        StringBuilder token = new StringBuilder(FIXED_PREFIX);

        // 필요한 자리수 계산
        int remainingLength = TOTAL_LENGTH - FIXED_PREFIX.length();

        // 랜덤 숫자 추가
        for (int i = 0; i < remainingLength; i++) {
            token.append(random.nextInt(10)); // 0~9 사이의 숫자 추가
        }

        return token.toString();
    }
}

