package com.qrroad.oqms.payment.gateway.dto;

import com.qrroad.oqms.payment.gateway.dto.trxinfo.TrxInfo;
import com.qrroad.oqms.payment.gateway.util.ToStringUtil;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MsgDto<T extends TrxInfo> {
    private PayInfo payInfo;
    private T trxInfo;
    private MsgResponse msgResponse;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}