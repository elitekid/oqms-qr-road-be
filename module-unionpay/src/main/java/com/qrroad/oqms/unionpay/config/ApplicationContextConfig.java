package com.qrroad.oqms.unionpay.config;

import com.qrroad.oqms.infrastructure.config.VaultConfig;
import com.qrroad.oqms.infrastructure.service.VaultService;
import com.qrroad.oqms.domain.config.DomainConfig;
import com.qrroad.oqms.infrastructure.config.DBConfig;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 이 클래스는 애플리케이션에서 필요한 도메인 및 인프라 관련 설정을 통합하여
 * 사용하는 역할을 합니다.
 * <p>
 * - `DomainConfig`, `DBConfig`, 'VaultConfig' 를 임포트하여 애플리케이션 내에서
 *   해당 빈들을 사용할 수 있도록 합니다.
 * <p>
 * 애플리케이션에 필요한 도메인, 인프라 빈을 모두 포함하는 설정 파일입니다.
 * <p>
 * <b>주의:</b> local 환경에서 실행할 경우 application-local.yaml에
 * 반드시 spring.datasource의 모든 항목(url, username, password 등)을 포함해야 합니다.
 * (local에서는 Spring Boot의 자동설정으로 DataSource가 생성됩니다)
 */
@Configuration
@Import({DomainConfig.class, DBConfig.class, VaultConfig.class})
@Slf4j
public class ApplicationContextConfig {
    private final VaultService vaultService;

    public ApplicationContextConfig(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @Bean
    @Profile("!local")
    public DataSource dataSource() {
        Map<String, Object> mysqlData = vaultService.getSecretData("/oqms/data/mysql");
        String userName = vaultService.decryptByDek(mysqlData.get("username").toString());
        String userPassword = vaultService.decryptByDek(mysqlData.get("userpassword").toString());
        String url = vaultService.decryptByDek(mysqlData.get("url").toString());

        log.info("userName: {}", userName);
        log.info("userPassword: {}", userPassword);
        log.info("url: {}", url);

        return org.springframework.boot.jdbc.DataSourceBuilder.create()
                .username(userName)
                .password(userPassword)
                .url(url)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}