package com.qrroad.oqms.payment.gateway.controller;

import com.qrroad.oqms.payment.gateway.factory.PaymentServiceFactory;
import com.qrroad.oqms.payment.gateway.service.PaymentGatewayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Enumeration;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/payment")
@RequiredArgsConstructor
public class GatewayController {
    private final RestTemplate restTemplate;
    private final PaymentGatewayService gatewayService;
    private final PaymentServiceFactory paymentServiceFactory;

    /**
     * 결제 요청
     *
     * @param request 요청
     * @param requestBody 요청데이터
     * @return 응답 데이터
     */
    @PostMapping(value = "/request",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> requestFromBankTokenRequest(HttpServletRequest request, @RequestBody Map<String, Object> requestBody) {
        String instCd = (String) requestBody.get("instCd");
        PaymentGatewayService service = paymentServiceFactory.getPaymentService(instCd);

        return service.processPayment(request, requestBody);
    }

    /**
     * 거래 내역 조회
     *
     * @param request 요청
     * @param requestBody 요청데이터
     * @return 응답 데이터
     */
    @PostMapping(value = "/update",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> requestFromBankTokenStateUpdate(HttpServletRequest request, @RequestBody Object requestBody) {
        return gatewayService.processCheckPayment(request, requestBody);
    }

    @RequestMapping(value = "/{provider}/**", method = RequestMethod.POST)
    public ResponseEntity<String> routePayment(@PathVariable String provider, HttpServletRequest request, @RequestBody String paymentDetails) {
        // 각 provider에 맞는 서비스 URL을 동적으로 가져오는 메서드
        String serviceUrl = getServiceUrlForProvider(provider);

        // 요청된 URI에서 '/payment/{provider}' 부분을 제외한 나머지 부분을 가져와서 전달
        String targetUrl = serviceUrl + request.getRequestURI().substring(("/payment/" + provider).length());

        // 원본 요청의 헤더를 복사하여 전달
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.add(headerName, request.getHeader(headerName));
        }

        // Content-Type 헤더를 설정
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HttpEntity 객체를 생성하여 헤더와 본문을 포함
        HttpEntity<String> httpEntity = new HttpEntity<>(paymentDetails, headers);

        // 요청을 해당 서비스로 전달
        return restTemplate.postForEntity(targetUrl, httpEntity, String.class);
    }

    private String getServiceUrlForProvider(String provider) {
        return switch (provider.toLowerCase()) {
            case "unionpay" -> "http://localhost:9101"; // UnionPay 서비스
            // 추가적인 페이사 서비스 추가 가능
            default -> throw new IllegalArgumentException("Invalid provider: " + provider);
        };
    }
}