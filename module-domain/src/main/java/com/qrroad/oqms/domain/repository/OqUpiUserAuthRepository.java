package com.qrroad.oqms.domain.repository;

import com.qrroad.oqms.domain.dto.OqUpiUserAuthDto;

public interface OqUpiUserAuthRepository {
    OqUpiUserAuthDto selectUserAuthInfo(String authCode);
    String selectNextParam(String txnID);
    void save(OqUpiUserAuthDto userAuthDto);
}
