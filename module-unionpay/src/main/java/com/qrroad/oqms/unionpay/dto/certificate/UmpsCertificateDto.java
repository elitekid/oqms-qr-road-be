package com.qrroad.oqms.unionpay.dto.certificate;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UmpsCertificateDto {
    private String umpsSignCertId;
    private String umpsSignPublicKey;
    private String umpsEncCertId;
    private String umpsEncPublicKey;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}
