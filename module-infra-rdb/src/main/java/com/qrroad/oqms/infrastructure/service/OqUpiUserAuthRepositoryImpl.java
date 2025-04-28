package com.qrroad.oqms.infrastructure.service;

import com.qrroad.oqms.domain.dto.OqUpiUserAuthDto;
import com.qrroad.oqms.domain.repository.OqUpiUserAuthRepository;
import com.qrroad.oqms.infrastructure.entity.OqUpiUserAuth;
import com.qrroad.oqms.infrastructure.repository.JpaOqUpiUserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OqUpiUserAuthRepositoryImpl implements OqUpiUserAuthRepository {
    private final JpaOqUpiUserAuthRepository jpaRepository;

    @Override
    public OqUpiUserAuthDto selectUserAuthInfo(String authCode) {
        return jpaRepository.findByAuthCode(authCode)
                .map(entity -> OqUpiUserAuthDto.builder()
                        .token(entity.getToken())
                        .mpqrcUrl(entity.getMpqrcUrl())
                        .appUserId(entity.getAppUserId()) // 필요한 필드에 매핑
                        .authCode(entity.getAuthCode())
                        .authStts(entity.getAuthStts())
                        .txnID(entity.getTxnID())
                        .trxAmt(entity.getTrxAmt())
                        .nextParam(entity.getNextParam())
                        .build()
                )
                .orElse(null); // 데이터가 없을 경우 null 반환
    }

    @Override
    public String selectNextParam(String txnID) {
        return jpaRepository.findNextParamByTxnID(txnID).orElse(null);
    }

    @Override
    public void save(OqUpiUserAuthDto userAuthDto) {
        try {
            OqUpiUserAuth entity = jpaRepository.findByAuthCodeAndToken(userAuthDto.getAuthCode(), userAuthDto.getToken())
                    .orElse(null);

            if(entity == null) {
                OqUpiUserAuth oqUpiUserAuth = OqUpiUserAuth.builder()
                        .token(userAuthDto.getToken())
                        .txnID(userAuthDto.getTxnID())
                        .mpqrcUrl(userAuthDto.getMpqrcUrl())
                        .authCode(userAuthDto.getAuthCode())
                        .build();

                jpaRepository.save(oqUpiUserAuth);
            } else {
                OqUpiUserAuth oqUpiUserAuth = entity.toBuilder()
                        .authStts("Y")
                        .appUserId(userAuthDto.getAppUserId())
                        .trxAmt(userAuthDto.getTrxAmt())
                        .nextParam(userAuthDto.getNextParam())
                        .build();

                jpaRepository.save(oqUpiUserAuth);
            }
        } catch (Exception e) {
            // 예외 처리
        }
    }
}
