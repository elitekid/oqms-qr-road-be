package com.qrroad.oqms.payment.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = {"com.qrroad.oqms"})
public class GatewayConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
