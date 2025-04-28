package com.qrroad.oqms.unionpay.dto.upitrxinfo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class KeyTrxInfo implements TrxInfo {
    private String umpsSignCertID;
    private String umpsSignPublicKey;
    private String appSignCertID;
    private String appSignPublicKey;
    private String umpsEncCertID;
    private String umpsEncPublicKey;
    private String appEncCertID;
    private String appEncPublicKey;
}
