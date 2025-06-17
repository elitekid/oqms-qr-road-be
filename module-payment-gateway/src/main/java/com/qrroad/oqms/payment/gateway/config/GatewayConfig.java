package com.qrroad.oqms.payment.gateway.config;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import com.qrroad.oqms.domain.config.DomainConfig;
import com.qrroad.oqms.infrastructure.config.DBConfig;
import com.qrroad.oqms.infrastructure.config.VaultConfig;
import com.qrroad.oqms.infrastructure.service.VaultService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Import({DomainConfig.class, DBConfig.class, VaultConfig.class})
@Slf4j
public class GatewayConfig {
    private final VaultService vaultService;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    public GatewayConfig(VaultService vaultService) {
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
