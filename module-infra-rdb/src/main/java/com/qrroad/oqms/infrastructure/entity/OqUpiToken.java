package com.qrroad.oqms.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "oq_upi_token", schema = "qrbgw")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OqUpiToken {
    @EmbeddedId
    private OqUpiTokenPK oqUpiTokenPK;

    @Column(name = "token", length = 19)
    private String token;

    @Column(name = "token_ref_id", length = 10)
    private String tokenRefId;

    @Column(name = "token_state", length = 15)
    private String tokenState;

    @Column(name = "token_expiry", length = 14)
    private String tokenExpiry;

    @Column(name = "pan", length = 2048)
    private String pan;

    @Column(name = "app_user_id", length = 64)
    private String appUserId;

    @Column(name = "reserved_mobile_no", length = 25)
    private String reservedMobileNo;

    @UpdateTimestamp
    @Column(name = "update_time")
    private Timestamp updateTime;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Timestamp createTime;
}
