package com.qrroad.oqms.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class PackagerFactory {
    
    @Value("${oqms.packager.config-path:config/iso8583.xml}")
    private String packagerConfigPath;
    
    private volatile ISOPackager cachedPackager;
    private final Object lock = new Object();
    
    /**
     * Returns the standard ISO8583 packager.
     * Loads once and returns the cached instance.
     */
    public ISOPackager getStandardPackager() {
        if (cachedPackager == null) {
            synchronized (lock) {
                if (cachedPackager == null) {
                    cachedPackager = loadStandardPackager();
                }
            }
        }
        return cachedPackager;
    }
    
    private ISOPackager loadStandardPackager() {
        try {
            log.info("Loading standard ISO8583 packager: {}", packagerConfigPath);
            
            ClassPathResource resource = new ClassPathResource(packagerConfigPath);
            if (!resource.exists()) {
                throw new IllegalStateException("Packager configuration file not found: " + packagerConfigPath);
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                GenericPackager packager = new GenericPackager();
                packager.readFile(inputStream);
                
                log.info("Successfully loaded standard packager");
                return packager;
            }
            
        } catch (Exception e) {
            log.error("Failed to load standard packager", e);
            throw new IllegalStateException("Failed to initialize ISO8583 packager", e);
        }
    }
}