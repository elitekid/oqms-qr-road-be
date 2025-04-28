package com.qrroad.oqms.unionpay.dto.certificate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class PrivateCertificateDto {
    private String appPrivateEncrypt2048;
    private String appPrivateSignature2048;
    private String appPublicEncrypt2048;
    private String appPublicSignature2048;
}
