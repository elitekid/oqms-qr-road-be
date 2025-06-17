package com.qrroad.oqms.infrastructure.repository;

import com.qrroad.oqms.infrastructure.entity.OqPayment;
import com.qrroad.oqms.infrastructure.entity.OqPaymentPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOqPaymentRepository extends JpaRepository<OqPayment, OqPaymentPK> {
}
