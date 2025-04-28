package com.qrroad.oqms.payment.gateway.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface PaymentGatewayService {
    ResponseEntity<?> processPayment(HttpServletRequest request, Object requestBody);
    ResponseEntity<?> processCheckPayment(HttpServletRequest request, Object requestBody);
    String getGatewayName();
}
