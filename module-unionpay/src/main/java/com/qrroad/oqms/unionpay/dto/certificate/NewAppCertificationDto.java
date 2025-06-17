package com.qrroad.oqms.unionpay.dto.certificate;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

@Getter
@NoArgsConstructor
public class NewAppCertificationDto {
    private BigInteger newAppSignCertID;
    private String newAppSignPublicKey;
    private BigInteger newAppEncCertId;
    private String newAppEncPublicKey;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}
