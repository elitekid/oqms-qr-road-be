package com.qrroad.oqms.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "oqms.tcp")
public class TcpProperties {
    private String defaultHost = "localhost";
    private int defaultPort = 8080;
    private int connectTimeoutMs = 30000;
    private int readTimeoutMs = 60000;
    private int writeTimeoutMs = 30000;
    private boolean keepAlive = true;
    private int maxConnections = 10;
    private int maxIdleTime = 300000;
    private boolean autoReconnect = true;
    private int reconnectIntervalMs = 5000;
    private int maxReconnectAttempts = 10;
    private int messageHeaderLength = 2;
    private String encoding = "UTF-8";

    // Application-specific TCP configurations
    private Map<String, TcpAppConfig> apps = new HashMap<>();

    @Data
    public static class TcpAppConfig {
        private String host;
        private int port;
        private int connectTimeoutMs;
        private int readTimeoutMs;
        private int writeTimeoutMs;
        private boolean keepAlive;
        private int maxConnections;
        private int maxIdleTime;
        private boolean autoReconnect;
        private int reconnectIntervalMs;
        private int maxReconnectAttempts;
        private int messageHeaderLength;
        private String encoding;
    }
}