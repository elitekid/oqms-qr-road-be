package com.qrroad.oqms.infrastructure.repository;

import com.qrroad.oqms.infrastructure.entity.OqUpiUserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOqUpiUserAuthRepository extends JpaRepository<OqUpiUserAuth, Long> {
    Optional<OqUpiUserAuth> findByAuthCode(String authCode);
    Optional<OqUpiUserAuth> findByAuthCodeAndToken(String authCode, String token);
    Optional<String> findNextParamByTxnID(String txnID);
}
