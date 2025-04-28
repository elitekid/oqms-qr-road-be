package com.qrroad.oqms.unionpay.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class CertificateSignature {
    private String umpsSignCertID;
    private String appSignCertID;
    private String umpsEncCertID;
    private String signature;
}
