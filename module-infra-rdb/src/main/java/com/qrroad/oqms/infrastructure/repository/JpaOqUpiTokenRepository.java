package com.qrroad.oqms.infrastructure.repository;

import com.qrroad.oqms.infrastructure.entity.OqUpiToken;
import com.qrroad.oqms.infrastructure.entity.OqUpiTokenPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOqUpiTokenRepository extends JpaRepository<OqUpiToken, OqUpiTokenPK> {
    @Query("SELECT t FROM OqUpiToken t WHERE t.token = :searchTerm OR t.id.deviceId = :searchTerm")
    Optional<OqUpiToken> findByTokenOrDeviceId(@Param("searchTerm") String searchTerm);
}
