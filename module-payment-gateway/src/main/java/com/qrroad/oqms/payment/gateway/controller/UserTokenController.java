package com.qrroad.oqms.payment.gateway.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qrroad.oqms.payment.gateway.dto.MsgDto;
import com.qrroad.oqms.payment.gateway.dto.trxinfo.UserTokenTrxInfo;
import com.qrroad.oqms.payment.gateway.service.UserTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/token")
@RequiredArgsConstructor
public class UserTokenController {
    private final UserTokenService userTokenService;

    /**
     * 토큰 발급 요청을 처리합니다.
     *
     * @param userTokenDto 요청 데이터
     * @return 다시 전송할 응답 데이터
     */
    @PostMapping(value = "/request",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> tokenRequest(@RequestBody MsgDto<UserTokenTrxInfo> userTokenDto) {
        return ResponseEntity.ok(userTokenService.processTokenRequest(userTokenDto));
    }

    /**
     * 
     * 토큰 상태를 업데이트합니다.
     *
     * @param userTokenDto 요청 데이터
     * @return 다시 전송할 응답 데이터
     */
    @PostMapping(value = "/update/state",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> tokenStateUpdate(@RequestBody MsgDto<UserTokenTrxInfo> userTokenDto){
        return ResponseEntity.ok(userTokenService.processTokenStateUpdate(userTokenDto));
    }

    /**
     * 토큰 유효기간을 업데이트합니다.
     *
     * @param userTokenDto 요청 데이터
     * @return 전송할 응답 데이터
     */
    @PostMapping(value = "/update/expiry",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> tokenExpiryUpdate(@RequestBody MsgDto<UserTokenTrxInfo> userTokenDto){
        return ResponseEntity.ok(userTokenService.processTokenExpiryUpdate(userTokenDto));
    }
}
