package com.qrroad.oqms.unionpay.controller;

import com.qrroad.oqms.unionpay.service.CertificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/certificate")
@RequiredArgsConstructor
public class CertificationController {
    private final CertificationService certificationService;

    /**
     * 신규 인증키 생성시 테이블 업데이트 및 변경 요청 (키 파일이 변경됐을 때 해당 요청필요)
     */
    @PostMapping(value = "/key/update",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateKey() {
        certificationService.processKeyInquiryExchange();
    }
}
