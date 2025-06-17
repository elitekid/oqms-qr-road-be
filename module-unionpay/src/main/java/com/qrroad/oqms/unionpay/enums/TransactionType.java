package com.qrroad.oqms.unionpay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum TransactionType {
    CPM("00", "CPM"),
    MPM_EMV("01", "MPM-EMVCo"),
    MPM_URL_BACKEND("02", "MPM-BackEnd"),
    MPM_URL_FRONTEND("03", "MPM-FrontEnd"),
    CANCEL("04", "취소"),;

    private final String code;
    private final String name;

    public static Map<String, String> getTransactionTypeMap() {
        return Arrays.stream(TransactionType.values())
                .collect(Collectors.toMap(TransactionType::getCode, TransactionType::getName));
    }
}
