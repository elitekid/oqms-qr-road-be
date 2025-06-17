package com.qrroad.oqms.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "oq_user_token", schema = "qrbgw")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@DynamicUpdate
public class OqUserToken {
    @Id
    @Column(name = "user_token", nullable = false, length = 19)
    private String userToken;

    @Column(name = "inst_cd", length = 3)
    private String instCd;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "app_user_id", length = 64)
    private String appUserId;

    @Column(name = "token_state", length = 5)
    private String tokenState;

    @Column(name = "token_expiry", length = 5)
    private String tokenExpiry;

    @UpdateTimestamp
    @Column(name = "update_time")
    private Timestamp updateTime;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Timestamp createTime;
}
