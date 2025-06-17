package com.qrroad.oqms.domain.repository;

import org.springframework.stereotype.Component;

import com.qrroad.oqms.domain.dto.OqPaymentDto;

@Component
public interface OqPaymentRepository {
    void save(OqPaymentDto oqPaymentDto);
}
