package com.qrroad.oqms.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class OqUpiApvTrxPK implements Serializable {
    @Serial
    private static final long serialVersionUID = -1014945382909029089L;

    @Column(name = "trx_dt", nullable = false)
    private String trxDt;

    @Column(name = "trx_tm", nullable = false)
    private String trxTm;

    @Column(name = "txn_id", nullable = false)
    private String txnId;
}
