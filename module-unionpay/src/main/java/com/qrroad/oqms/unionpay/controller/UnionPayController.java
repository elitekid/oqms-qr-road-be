package com.qrroad.oqms.unionpay.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrroad.oqms.unionpay.dto.MsgInfo;
import com.qrroad.oqms.unionpay.dto.UpiMsgDto;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.GetUserIdTrxInfo;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.TokenTrxInfo;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.TrxStatusNotificationTrxInfo;
import com.qrroad.oqms.unionpay.service.MPMFrontService;
import com.qrroad.oqms.unionpay.service.TokenService;
import com.qrroad.oqms.unionpay.service.TrxStatusNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("umps")
@RequiredArgsConstructor
public class UnionPayController {
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final MPMFrontService mpmFrontService;
    private final TrxStatusNotificationService trxStatusNotificationService;

    /**
     * UPI 로부터 수신된 요청을 처리합니다.
     *
     * @param requestJson 은행으로부터의 요청 데이터
     * @return 은행에 다시 전송할 응답 데이터
     */
    @PostMapping(value = "/receive",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> receive(@RequestBody String requestJson) throws JsonProcessingException {
        Map<String, Object> requestMap = objectMapper.readValue(requestJson, new TypeReference<>() {});

        // MsgInfo 에서 MsgType 을 확인
        MsgInfo msgInfo = objectMapper.convertValue(requestMap.get("msgInfo"), MsgInfo.class);
        String msgType = msgInfo.getMsgType();

        // MsgType 에 따른 DTO 매핑
        return switch (msgType) {
            case "TOKEN_STATE_NOTIFICATION" -> {
                UpiMsgDto<TokenTrxInfo> tokenStateNotification = objectMapper.readValue(requestJson, new TypeReference<>() {});
                yield ResponseEntity.ok(tokenService.processTokenStateNotification(tokenStateNotification));
            }
            case "TRX_STATUS_NOTIFICATION" -> {
                UpiMsgDto<TrxStatusNotificationTrxInfo> trxStatusNotification = objectMapper.readValue(requestJson, new TypeReference<>() {});
                yield ResponseEntity.ok(trxStatusNotificationService.processTrxStatusNotification(trxStatusNotification));
            }
            case "GET_USER_ID" -> {
                UpiMsgDto<GetUserIdTrxInfo> getUserIdNotification = objectMapper.readValue(requestJson, new TypeReference<>() {});
                yield ResponseEntity.ok(mpmFrontService.getUserId(getUserIdNotification));
            }
            default -> ResponseEntity.badRequest().body("Invalid msgType");
        };
    }
}
