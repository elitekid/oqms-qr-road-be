package com.qrroad.oqms.unionpay.dto;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MsgInfo {
    private String versionNo;
    private String msgType;
    private String msgID;
    private String timeStamp;
    private String walletID;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}
