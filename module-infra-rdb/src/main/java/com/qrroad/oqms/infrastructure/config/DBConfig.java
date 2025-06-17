package com.qrroad.oqms.infrastructure.config;

import com.p6spy.engine.spy.P6SpyOptions;
import com.qrroad.oqms.infrastructure.decorator.P6SpyFormatter;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.qrroad.oqms.infrastructure.entity")
@EnableJpaRepositories(basePackages = "com.qrroad.oqms.infrastructure.repository")
@ComponentScan(basePackages = {"com.qrroad.oqms.infrastructure"})
public class DBConfig {

    private final EntityManager entityManager;

    public DBConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(P6SpyFormatter.class.getName());
    }
}
