package com.qrroad.oqms.unionpay.config;

import com.qrroad.oqms.infrastructure.config.VaultConfig;
import com.qrroad.oqms.infrastructure.service.VaultService;
import com.qrroad.oqms.domain.config.DomainConfig;
import com.qrroad.oqms.infrastructure.config.DBConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
 */
@Configuration
@Import({DomainConfig.class, DBConfig.class, VaultConfig.class})
@Slf4j
public class ApplicationContextConfig {
    // 애플리케이션에 필요한 도메인 및 인프라 설정을 모은 클래스
    private final VaultService vaultService;

    public ApplicationContextConfig(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource dataSource() {
        Map<String, Object> mysqlData = vaultService.getSecretData("/oqms/data/mysql");

        String userName = vaultService.decryptByDek(mysqlData.get("username").toString());
        String userPassword = vaultService.decryptByDek(mysqlData.get("userpassword").toString());

        log.info("userName: {}", userName);
        log.info("userPassword: {}", userPassword);

        return DataSourceBuilder.create()
                .username("sgryu01")
                .password("Qwer1324!@")
                .url("jdbc:mysql://10.47.4.10:43306/qrbgw?useSSL=false&allowPublicKeyRetrieval=true")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}