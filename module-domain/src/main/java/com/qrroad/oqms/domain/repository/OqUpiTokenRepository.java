package com.qrroad.oqms.domain.repository;

import com.qrroad.oqms.domain.dto.OqUpiTokenDto;
import org.springframework.stereotype.Component;

@Component
public interface OqUpiTokenRepository {
    void save(OqUpiTokenDto tokenDto);
    String getDeviceId(String token);
    String getInstCd(String token);
    OqUpiTokenDto getTokenByTokenOrDeviceId(String deviceId);
}
