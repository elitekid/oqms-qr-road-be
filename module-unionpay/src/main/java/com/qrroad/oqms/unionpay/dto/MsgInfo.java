package com.qrroad.oqms.unionpay.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class MsgInfo {
    private String versionNo;
    private String msgType;
    private String msgID;
    private String timeStamp;
    private String walletID;
}
