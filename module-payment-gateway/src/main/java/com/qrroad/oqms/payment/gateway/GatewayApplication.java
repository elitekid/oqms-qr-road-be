package com.qrroad.oqms.payment.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.qrroad.oqms.payment.gateway.config.GatewayConfig;

@SpringBootApplication
@Import(GatewayConfig.class)
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}