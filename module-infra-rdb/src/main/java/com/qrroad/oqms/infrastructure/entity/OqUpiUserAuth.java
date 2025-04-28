package com.qrroad.oqms.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "oq_upi_user_auth", schema = "qrbgw")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OqUpiUserAuth implements Serializable {
    @Serial
    private static final long serialVersionUID = 6858766914390771920L;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, length = 19)
    private String token;

    @Column(name = "mpqrc_url", nullable = false, length = 2048)
    private String mpqrcUrl;

    @Column(name = "app_user_id", length = 64)
    private String appUserId;

    @Column(name = "auth_code", nullable = false, length = 48)
    private String authCode;

    @Column(name = "auth_stts", nullable = false, length = 1)
    private String authStts;

    @Column(name = "txn_id", length = 49)
    private String txnID;

    @Column(name = "trx_amt", precision = 13, scale = 2)
    private BigDecimal trxAmt;

    @Column(name = "next_param", nullable = false, length = 2048)
    private String nextParam;

    @Column(name = "update_time")
    private Timestamp updateTime;

    @Column(name = "create_time")
    private Timestamp createTime;
}

