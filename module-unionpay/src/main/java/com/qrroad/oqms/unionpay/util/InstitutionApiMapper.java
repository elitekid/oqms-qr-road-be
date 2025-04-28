package com.qrroad.oqms.unionpay.util;

import com.qrroad.oqms.core.enums.InstitutionCode;
import com.qrroad.oqms.unionpay.enums.ApiSource;

import java.util.Arrays;

public class InstitutionApiMapper {
    public static InstitutionCode getInstitutionByCode(String code) {
        return Arrays.stream(InstitutionCode.values())
                .filter(institution -> institution.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Institution Code: " + code));
    }

    public static String getApiUrlByInstitutionCode(String institutionCode) {
        InstitutionCode institution = getInstitutionByCode(institutionCode);

        // 여기서 기관 코드와 ApiSource 간의 매핑 규칙 추가
        return switch (institution) {
            case WOORI_BANK -> ApiSource.WOORI_BANK.getUrl();
            case HANA_BANK -> ApiSource.HANA_BANK.getUrl();
            default -> ApiSource.UNKNOWN.getUrl();
        };
    }
}
