package com.qrroad.oqms.domain.repository;

import org.springframework.stereotype.Component;

import com.qrroad.oqms.domain.dto.OqUserTokenDto;

@Component
public interface OqUserTokenRepository {
    void save(OqUserTokenDto tokenDto);
    OqUserTokenDto getUserToken(String token);
}