package com.qrroad.oqms.payment.gateway.factory;

import com.qrroad.oqms.payment.gateway.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Lazy
@Component
@RequiredArgsConstructor
public class PaymentServiceFactory {
    private final Map<String, PaymentGatewayService> paymentGatewayServices;

    @Autowired
    public PaymentServiceFactory(List<PaymentGatewayService> services) {
        paymentGatewayServices = new HashMap<>();
        for (PaymentGatewayService service : services) {
            paymentGatewayServices.put(service.getGatewayName(), service);
            log.info("Service registered: {}", service.getGatewayName());
        }
    }

    public PaymentGatewayService getPaymentService(String gateway) {
        return paymentGatewayServices.get(gateway);

    }
}