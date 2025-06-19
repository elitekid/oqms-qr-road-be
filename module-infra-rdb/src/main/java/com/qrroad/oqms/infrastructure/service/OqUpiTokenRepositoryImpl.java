package com.qrroad.oqms.infrastructure.service;

import com.qrroad.oqms.domain.dto.OqUpiTokenDto;
import com.qrroad.oqms.domain.repository.OqUpiTokenRepository;
import com.qrroad.oqms.infrastructure.entity.OqUpiToken;
import com.qrroad.oqms.infrastructure.entity.OqUpiTokenPK;
import com.qrroad.oqms.infrastructure.entity.QOqUpiToken;
import com.qrroad.oqms.infrastructure.repository.JpaOqUpiTokenRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OqUpiTokenRepositoryImpl implements OqUpiTokenRepository {
    private final JpaOqUpiTokenRepository jpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void save(OqUpiTokenDto tokenDto) {
        try {
            OqUpiTokenPK tokenPk = OqUpiTokenPK.builder()
                    .instCd(tokenDto.getInstCd())
                    .deviceId(tokenDto.getDeviceId())
                    .build();

            OqUpiToken token = OqUpiToken.builder()
                    .oqUpiTokenPK(tokenPk)
                    .token(tokenDto.getToken())
                    .tokenRefId(tokenDto.getTokenRefId())
                    .tokenState(tokenDto.getTokenState())
                    .tokenExpiry(tokenDto.getTokenExpiry())
                    .pan(tokenDto.getPan())
                    .appUserId(tokenDto.getAppUserId())
                    .reservedMobileNo(tokenDto.getReservedMobileNo())
                    .build();

            jpaRepository.save(token);

        } catch (Exception e) {
            // 예외 처리
        }
    }

    @Override
    public String getDeviceId(String token) {
        QOqUpiToken qOqUpiToken = QOqUpiToken.oqUpiToken;

        return jpaQueryFactory
                .select(qOqUpiToken.oqUpiTokenPK.deviceId)
                .from(qOqUpiToken)
                .where(qOqUpiToken.token.eq(token)).fetchOne();
    }

    @Override
    public String getInstCd(String token) {
        QOqUpiToken qOqUpiToken = QOqUpiToken.oqUpiToken;

        return jpaQueryFactory
                .select(qOqUpiToken.oqUpiTokenPK.instCd)
                .from(qOqUpiToken)
                .where(qOqUpiToken.token.eq(token)).fetchOne();
    }

    @Override
    public OqUpiTokenDto getTokenByTokenOrDeviceId(String searchTerm) {
        return jpaRepository.findByTokenOrDeviceId(searchTerm)
                .map(token -> OqUpiTokenDto.builder()
                        .instCd(token.getOqUpiTokenPK().getInstCd())
                        .deviceId(token.getOqUpiTokenPK().getDeviceId())
                        .token(token.getToken())
                        .tokenRefId(token.getTokenRefId())
                        .tokenState(token.getTokenState())
                        .tokenExpiry(token.getTokenExpiry())
                        .pan(token.getPan())
                        .appUserId(token.getAppUserId())
                        .reservedMobileNo(token.getReservedMobileNo())
                        .build())
                .orElse(null);
    }
}
