package com.qrroad.oqms.unionpay.dto.certificate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class AppCertificateDto {
    private String appSignCertId;
    private String appSignPublicKey;
    private String appEncCertId;
    private String appEncPublicKey;
}
