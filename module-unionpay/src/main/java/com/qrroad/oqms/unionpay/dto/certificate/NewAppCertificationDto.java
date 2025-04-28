package com.qrroad.oqms.unionpay.dto.certificate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@ToString
public class NewAppCertificationDto {
    private BigInteger newAppSignCertID;
    private String newAppSignPublicKey;
    private BigInteger newAppEncCertId;
    private String newAppEncPublicKey;
}
