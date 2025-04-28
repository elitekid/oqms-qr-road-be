package com.qrroad.oqms.unionpay.dto.certificate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class UmpsCertificateDto {
    private String umpsSignCertId;
    private String umpsSignPublicKey;
    private String umpsEncCertId;
    private String umpsEncPublicKey;
}
