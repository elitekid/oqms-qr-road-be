package com.qrroad.oqms.unionpay.controller;

import com.qrroad.oqms.unionpay.dto.ApiMsgDto;
import com.qrroad.oqms.unionpay.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService service;

    /**
     * 은행으로부터 수신된 토큰 발급 요청을 처리합니다.
     *
     * @param dto 은행으로부터의 요청 데이터
     * @return 은행에 다시 전송할 응답 데이터
     */
    @PostMapping(value = "/request",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMsgDto tokenRequest(@RequestBody ApiMsgDto dto) {
        return service.processTokenRequest(dto);
    }

    /**
     * UPI 으로부터 수신된 토큰 상태를 업데이트합니다.
     *
     * @param dto 은행으로부터의 요청 데이터
     * @return 은행에 다시 전송할 응답 데이터
     */
    @PostMapping(value = "/update",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMsgDto tokenStateUpdate(@RequestBody ApiMsgDto dto) {
        return service.processTokenStateUpdate(dto);
    }

}
