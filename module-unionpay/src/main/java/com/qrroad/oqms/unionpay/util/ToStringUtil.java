package com.qrroad.oqms.unionpay.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ToStringUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    public static String toPrettyString(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return obj.getClass().getSimpleName() + " " + objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString(); // JSON 변환 실패 시 기본 toString() 사용
        }
    }
}
