package com.qrroad.oqms.unionpay.dto;

import com.qrroad.oqms.unionpay.dto.upitrxinfo.TrxInfo;
import com.qrroad.oqms.unionpay.util.ToStringUtil;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class UpiMsgDto<T extends TrxInfo> {
    private MsgInfo msgInfo;
    private T trxInfo;
    private MsgResponse msgResponse;
    private CertificateSignature certificateSignature;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}
