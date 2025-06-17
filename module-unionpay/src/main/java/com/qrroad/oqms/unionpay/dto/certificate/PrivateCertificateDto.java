package com.qrroad.oqms.unionpay.dto.certificate;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PrivateCertificateDto {
    private String appPrivateEncrypt2048;
    private String appPrivateSignature2048;
    private String appPublicEncrypt2048;
    private String appPublicSignature2048;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}
