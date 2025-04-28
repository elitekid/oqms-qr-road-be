package com.qrroad.oqms.unionpay.dto;

import com.qrroad.oqms.unionpay.dto.upitrxinfo.TrxInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class UpiMsgDto<T extends TrxInfo> {
    private MsgInfo msgInfo;
    private T trxInfo;
    private MsgResponse msgResponse;
    private CertificateSignature certificateSignature;
}
