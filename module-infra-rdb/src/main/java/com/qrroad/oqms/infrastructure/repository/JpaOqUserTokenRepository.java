package com.qrroad.oqms.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qrroad.oqms.infrastructure.entity.OqUserToken;

@Repository
public interface JpaOqUserTokenRepository extends JpaRepository<OqUserToken, String> {
}
