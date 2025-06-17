package com.qrroad.oqms.infrastructure.service;

import com.qrroad.oqms.domain.dto.OqUserTokenDto;
import com.qrroad.oqms.domain.repository.OqUserTokenRepository;
import com.qrroad.oqms.infrastructure.entity.OqUserToken;
import com.qrroad.oqms.infrastructure.repository.JpaOqUserTokenRepository;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OqUserTokenRepositoryImpl implements OqUserTokenRepository {
    private final JpaOqUserTokenRepository jpaRepository;

    @Override
    public void save(OqUserTokenDto dto) {
        try {
            Optional<OqUserToken> existingEntityOpt = jpaRepository.findById(dto.getUserToken());

            if (existingEntityOpt.isPresent()) {
                OqUserToken existingEntity = existingEntityOpt.get();

                // null이나 ""이 아닌 값만 업데이트 (기존 값 유지)
                OqUserToken updatedEntity = existingEntity.toBuilder()
                    .instCd((dto.getInstCd() != null && !dto.getInstCd().isEmpty()) ? dto.getInstCd() : existingEntity.getInstCd())
                    .deviceId((dto.getDeviceId() != null && !dto.getDeviceId().isEmpty()) ? dto.getDeviceId() : existingEntity.getDeviceId())
                    .appUserId((dto.getAppUserId() != null && !dto.getAppUserId().isEmpty()) ? dto.getAppUserId() : existingEntity.getAppUserId())
                    .tokenState((dto.getTokenState() != null && !dto.getTokenState().isEmpty()) ? dto.getTokenState() : existingEntity.getTokenState())
                    .tokenExpiry((dto.getTokenExpiry() != null && !dto.getTokenExpiry().isEmpty()) ? dto.getTokenExpiry() : existingEntity.getTokenExpiry())
                    .updateTime(dto.getUpdateTime() != null ? dto.getUpdateTime() : existingEntity.getUpdateTime())
                    .createTime(dto.getCreateTime() != null ? dto.getCreateTime() : existingEntity.getCreateTime())
                    .build();

                jpaRepository.save(updatedEntity); // 기존 값 유지하면서 변경된 필드만 업데이트
            } else {
                // DTO를 엔티티로 변환하여 저장
                OqUserToken newEntity = OqUserToken.builder()
                    .userToken(dto.getUserToken())
                    .instCd(dto.getInstCd())
                    .deviceId(dto.getDeviceId())
                    .appUserId(dto.getAppUserId())
                    .tokenState(dto.getTokenState())
                    .tokenExpiry(dto.getTokenExpiry())
                    .updateTime(dto.getUpdateTime())
                    .createTime(dto.getCreateTime())
                    .build();

                jpaRepository.save(newEntity);
            }
        } catch (Exception e) {
            System.err.println("토큰 저장 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public OqUserTokenDto getUserToken(String token) {
        return jpaRepository.findById(token).map(
            entity -> OqUserTokenDto.builder()
                .userToken(entity.getUserToken())
                .instCd(entity.getInstCd())
                .deviceId(entity.getDeviceId())
                .appUserId(entity.getAppUserId())
                .tokenState(entity.getTokenState())
                .tokenExpiry(entity.getTokenExpiry())
                .updateTime(entity.getUpdateTime())
                .createTime(entity.getCreateTime())
                .build())
            .orElse(null); // 존재하지 않으면 null 반환
    }
}