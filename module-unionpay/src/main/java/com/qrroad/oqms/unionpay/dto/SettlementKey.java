package com.qrroad.oqms.unionpay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qrroad.oqms.unionpay.util.ToStringUtil;

import lombok.Builder;

@Builder(toBuilder = true)
public class SettlementKey {

    private String acquirerIIN;
    private String forwardingIIN;
    private String systemTraceAuditNumber;
    @JsonProperty("TransmissionDateTime")
    private String transmissionDateTime;

    public String getAcquirerIIN() {
        return acquirerIIN;
    }

    public void setAcquirerIIN(String acquirerIIN) {
        this.acquirerIIN = acquirerIIN;
    }

    public String getForwardingIIN() {
        return forwardingIIN;
    }

    public void setForwardingIIN(String forwardingIIN) {
        this.forwardingIIN = forwardingIIN;
    }

    public String getSystemTraceAuditNumber() {
        return systemTraceAuditNumber;
    }

    public void setSystemTraceAuditNumber(String systemTraceAuditNumber) {
        this.systemTraceAuditNumber = systemTraceAuditNumber;
    }

    @JsonProperty("TransmissionDateTime")
    public String getTransmissionDateTime() {
        return transmissionDateTime;
    }

    @JsonProperty("TransmissionDateTime")
    public void setTransmissionDateTime(String transmissionDateTime) {
        this.transmissionDateTime = transmissionDateTime;
    }

    @Override
    public String toString() {
        return ToStringUtil.toPrettyString(this);
    }

}
