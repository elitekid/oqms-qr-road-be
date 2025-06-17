package com.qrroad.oqms.unionpay.dto;

import com.qrroad.oqms.unionpay.util.ToStringUtil;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class RiskInfo {
    private String gps;
    private String[] simCard;
    private String appUserID;
    private String usrEnrolDate;
    private String captureMethod;
    private String ipAddress;
    private String reservedMobileNo;
    private String deviceType;
    private String deviceScore;

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }
}
