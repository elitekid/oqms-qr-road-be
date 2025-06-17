package com.qrroad.oqms.unionpay.dto.certificate;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AppCertificateDto {
    private String appSignCertId;
    private String appSignPublicKey;
    private String appEncCertId;
    private String appEncPublicKey;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}
