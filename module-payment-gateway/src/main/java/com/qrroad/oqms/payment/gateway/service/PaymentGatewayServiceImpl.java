package com.qrroad.oqms.payment.gateway.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService{
    @Override
    public ResponseEntity<?> processPayment(HttpServletRequest request, Object requestBody) {
        return null;
    }

    @Override
    public ResponseEntity<?> processCheckPayment(HttpServletRequest request, Object requestBody) {
        return null;
    }

    @Override
    public String getGatewayName() {
        return "gateway";
    }
}
