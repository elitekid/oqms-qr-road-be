package com.qrroad.oqms.unionpay.dto.upitrxinfo;

import com.qrroad.oqms.unionpay.dto.RiskInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class TokenTrxInfo implements TrxInfo {
    private String deviceID;
    private String pan;
    private String[] useCaseIndicator;
    private String expiryDate;
    private String[] cvm;
    private HceInfo hceInfo;
    private RiskInfo riskInfo;
    private String token;
    private String tokenRefID;
    private String maskedPAN;
    private String maskedToken;
    private String par;
    private String tokenState;
    private String tokenExpiry;

    // TOKEN_STATE_NOTIFICATION
    private String srcDigitalCardId;
    private String srcCorrelationId;
    private EncryptedConsumerIdentity encryptedConsumerIdentity;

    // TOKEN_META_UPDATE
    private String enrolID;
    private String tokenAction;

    @Getter
    @Builder(toBuilder = true)
    public static class HceInfo {
        private String sdkProperties;
        private String cardInfo;
    }

    @Getter
    @Builder(toBuilder = true)
    public static class EncryptedConsumerIdentity {
        private String identityProvider;
        private String identityType;
        private String identityValue;
    }
}
