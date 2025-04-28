package com.qrroad.oqms.unionpay.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
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
}
