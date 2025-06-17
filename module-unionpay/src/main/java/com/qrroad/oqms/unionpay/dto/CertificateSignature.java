package com.qrroad.oqms.unionpay.dto;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class CertificateSignature {
    private String umpsSignCertID;
    private String appSignCertID;
    private String umpsEncCertID;
    private String signature;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}
