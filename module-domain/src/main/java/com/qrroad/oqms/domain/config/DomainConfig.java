package com.qrroad.oqms.domain.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = "com.qrroad.oqms.domain.entity")
@ComponentScan("com.qrroad.oqms.domain.repository")
public class DomainConfig {
    // 해당 Config 는 컴포넌트, 엔티티 등을 Domain 의존성 모듈에서 사용하기 위함.
}
