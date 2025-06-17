package com.qrroad.oqms.domain.repository;

import org.springframework.stereotype.Component;

import com.qrroad.oqms.domain.dto.OqUpiUserAuthDto;
@Component
public interface OqUpiUserAuthRepository {
    OqUpiUserAuthDto selectUserAuthInfo(String authCode);
    String selectNextParam(String txnID);
    void save(OqUpiUserAuthDto userAuthDto);
}
