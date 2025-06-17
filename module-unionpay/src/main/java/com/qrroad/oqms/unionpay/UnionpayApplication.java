package com.qrroad.oqms.unionpay;

import com.qrroad.oqms.unionpay.config.ApplicationContextConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ApplicationContextConfig.class)
public class UnionpayApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnionpayApplication.class, args);
    }
}