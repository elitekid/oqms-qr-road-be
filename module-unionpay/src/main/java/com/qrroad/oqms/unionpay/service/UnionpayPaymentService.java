package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.payment.gateway.service.PaymentGatewayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnionpayPaymentService implements PaymentGatewayService {
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
        return "unionpay";
    }
}