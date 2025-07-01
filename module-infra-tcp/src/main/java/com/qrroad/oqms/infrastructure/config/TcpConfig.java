package com.qrroad.oqms.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.qrroad.oqms.infrastructure.tcp")
public class TcpConfig {
    
    /**
     * TCP 모듈 설정 정보
     * 
     * 주요 기능:
     * - ISO8583 패키저 자동 로드
     * - 연결 풀 관리
     * - 자동 재연결
     * - 헬스체크
     */
}