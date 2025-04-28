package com.qrroad.oqms.unionpay.controller;

import com.qrroad.oqms.unionpay.service.CPMService;
import com.qrroad.oqms.unionpay.service.TrxStatusNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/umps/cpm")
@RequiredArgsConstructor
public class CPMController {

//    private final CPMService cpmService;
//    private final TrxStatusNotificationService trxStatusNotificationService;
//
//
//    /**
//     * 은행으로부터 수신된 토큰 상태를 업데이트합니다.
//     *
//     * @param dto 은행으로부터의 요청 데이터
//     * @return 은행에 다시 전송할 응답 데이터
//     */
//	@PostMapping(value = "/request",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public CpqrcGenerationBankDto requestFromBankCpqrcGeneration(@RequestBody CpqrcGenerationBankDto dto) throws Exception {
//
//        CpqrcGenerationBankDto response = cpmService.processCpqrcGeneration(dto);
//
//        return response;
//    }
//    /**
//     * 은행으로부터 수신된 토큰 상태를 업데이트합니다.
//     *
//     * @param dto 은행으로부터의 요청 데이터
//     * @return 은행에 다시 전송할 응답 데이터
//     */
//    @PostMapping(value = "/noti",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public TrxStatusNotificationUpiDto trxStatusNotificationFromUPI(TrxStatusNotificationUpiDto dto){
//
//        TrxStatusNotificationUpiDto response = trxStatusNotificationService.processTrxStatusNotification(dto);
//
//        return response;
//    }

}
