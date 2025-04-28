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
public class OqUpiTokenPK implements Serializable {
    @Serial
    private static final long serialVersionUID = 2246904914363936003L;

    @Column(name = "inst_cd", nullable = false, length = 3)
    private String instCd;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;
}
