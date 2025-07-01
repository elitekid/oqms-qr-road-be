package com.qrroad.oqms.unionpay.dto.iso8583;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * ISO8583 전문 헤더 섹션
 * 
 * 불변 객체로 설계된 ISO8583 메시지 헤더
 * - Builder 패턴을 통한 안전한 객체 생성
 * - 바이트 레벨 직렬화/역직렬화 지원
 * - Thread-safe한 불변 구조
 */
@Builder(toBuilder = true)
@Slf4j
public class ISO8583Header {

    // === 헤더 필드 정의 ===
    
    /**
     * 헤더 길이 (1바이트)
     * 전체 헤더의 바이트 길이를 나타냄
     */
    private final byte headerLength;

    /**
     * 헤더 플래그 및 버전 (1바이트)
     * 비트별로 다음 정보를 포함:
     * - 비트 7-4: 헤더 버전
     * - 비트 3-0: 플래그 정보
     */
    private final byte headerFlagAndVersion;

    /**
     * 전체 메시지 길이 (4바이트)
     * 헤더를 포함한 전체 메시지의 바이트 길이
     */
    private final int totalMessageLength;

    /**
     * 목적지 ID (11바이트)
     * 메시지를 수신할 기관의 식별자
     */
    private final String destinationId;

    /**
     * 출발지 ID (11바이트)
     * 메시지를 송신하는 기관의 식별자
     */
    private final String sourceId;

    /**
     * 예약 필드 (3바이트)
     * 향후 확장을 위한 예약 공간
     */
    private final byte[] reserved;

    /**
     * 배치 번호 (1바이트)
     * 배치 처리 시 사용되는 순번
     */
    private final byte batchNumber;

    /**
     * 거래 정보 (8바이트)
     * 거래 관련 추가 정보나 라우팅 정보
     */
    private final String transactionInfo;

    /**
     * 사용자 정보 (1바이트)
     * 사용자 구분이나 권한 관련 정보
     */
    private final byte userInfo;

    /**
     * 거부 코드 (5바이트)
     * 메시지 처리 실패 시의 거부 사유 코드
     */
    private final String rejectCode;

    // === 필드 길이 상수 ===
    private static final int HEADER_LENGTH_SIZE = 1;
    private static final int HEADER_FLAG_VERSION_SIZE = 1;
    private static final int TOTAL_MSG_LENGTH_SIZE = 4;
    private static final int DEST_ID_SIZE = 11;
    private static final int SRC_ID_SIZE = 11;
    private static final int RESERVED_SIZE = 3;
    private static final int BATCH_NO_SIZE = 1;
    private static final int TRX_INFO_SIZE = 8;
    private static final int USER_INFO_SIZE = 1;
    private static final int REJECT_CODE_SIZE = 5;
    
    /**
     * 헤더 전체 크기 (고정)
     */
    public static final int TOTAL_HEADER_SIZE = 
        HEADER_LENGTH_SIZE + HEADER_FLAG_VERSION_SIZE + TOTAL_MSG_LENGTH_SIZE +
        DEST_ID_SIZE + SRC_ID_SIZE + RESERVED_SIZE + BATCH_NO_SIZE +
        TRX_INFO_SIZE + USER_INFO_SIZE + REJECT_CODE_SIZE;

    /**
     * 바이트 배열로부터 ISO8583Header 객체 생성
     * 
     * @param headerBytes 헤더 바이트 배열 (45바이트)
     * @return ISO8583Header 객체
     * @throws IllegalArgumentException 바이트 배열 길이가 잘못된 경우
     */
    public static ISO8583Header fromBytes(byte[] headerBytes) {
        if (headerBytes == null || headerBytes.length != TOTAL_HEADER_SIZE) {
            throw new IllegalArgumentException(
                String.format("Header bytes must be exactly %d bytes, but was %d", 
                    TOTAL_HEADER_SIZE, headerBytes != null ? headerBytes.length : 0));
        }

        ByteBuffer buffer = ByteBuffer.wrap(headerBytes).order(ByteOrder.BIG_ENDIAN);
        
        try {
            return ISO8583Header.builder()
                .headerLength(buffer.get())
                .headerFlagAndVersion(buffer.get())
                .totalMessageLength(buffer.getInt())
                .destinationId(readStringField(buffer, DEST_ID_SIZE))
                .sourceId(readStringField(buffer, SRC_ID_SIZE))
                .reserved(readByteArray(buffer, RESERVED_SIZE))
                .batchNumber(buffer.get())
                .transactionInfo(readStringField(buffer, TRX_INFO_SIZE))
                .userInfo(buffer.get())
                .rejectCode(readStringField(buffer, REJECT_CODE_SIZE))
                .build();
                
        } catch (Exception e) {
            log.error("Failed to parse ISO8583Header from bytes", e);
            throw new IllegalArgumentException("Invalid header format", e);
        }
    }

    /**
     * ISO8583Header 객체를 바이트 배열로 직렬화
     * 
     * @return 헤더 바이트 배열 (45바이트)
     */
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(TOTAL_HEADER_SIZE).order(ByteOrder.BIG_ENDIAN);
        
        try {
            buffer.put(headerLength);
            buffer.put(headerFlagAndVersion);
            buffer.putInt(totalMessageLength);
            writeStringField(buffer, destinationId, DEST_ID_SIZE);
            writeStringField(buffer, sourceId, SRC_ID_SIZE);
            writeByteArray(buffer, reserved, RESERVED_SIZE);
            buffer.put(batchNumber);
            writeStringField(buffer, transactionInfo, TRX_INFO_SIZE);
            buffer.put(userInfo);
            writeStringField(buffer, rejectCode, REJECT_CODE_SIZE);
            
            return buffer.array();
            
        } catch (Exception e) {
            log.error("Failed to serialize ISO8583Header to bytes", e);
            throw new IllegalStateException("Failed to serialize header", e);
        }
    }

    /**
     * 헤더 유효성 검증
     * 
     * @return 유효한 경우 true, 그렇지 않으면 false
     */
    public boolean isValid() {
        try {
            // 필수 필드 검증
            if (destinationId == null || destinationId.trim().isEmpty()) {
                log.warn("Invalid header: destinationId is null or empty");
                return false;
            }
            
            if (sourceId == null || sourceId.trim().isEmpty()) {
                log.warn("Invalid header: sourceId is null or empty");
                return false;
            }
            
            // 길이 검증
            if (destinationId.length() > DEST_ID_SIZE) {
                log.warn("Invalid header: destinationId too long ({})", destinationId.length());
                return false;
            }
            
            if (sourceId.length() > SRC_ID_SIZE) {
                log.warn("Invalid header: sourceId too long ({})", sourceId.length());
                return false;
            }
            
            if (transactionInfo != null && transactionInfo.length() > TRX_INFO_SIZE) {
                log.warn("Invalid header: transactionInfo too long ({})", transactionInfo.length());
                return false;
            }
            
            if (rejectCode != null && rejectCode.length() > REJECT_CODE_SIZE) {
                log.warn("Invalid header: rejectCode too long ({})", rejectCode.length());
                return false;
            }
            
            // 예약 필드 길이 검증
            if (reserved != null && reserved.length != RESERVED_SIZE) {
                log.warn("Invalid header: reserved field wrong size ({})", reserved.length);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error validating header", e);
            return false;
        }
    }

    // === 접근자 메서드 (Getter 대신 명시적 메서드) ===
    
    public byte headerLength() { return headerLength; }
    public byte headerFlagAndVersion() { return headerFlagAndVersion; }
    public int totalMessageLength() { return totalMessageLength; }
    public String destinationId() { return destinationId; }
    public String sourceId() { return sourceId; }
    public byte[] reserved() { return reserved != null ? Arrays.copyOf(reserved, reserved.length) : null; }
    public byte batchNumber() { return batchNumber; }
    public String transactionInfo() { return transactionInfo; }
    public byte userInfo() { return userInfo; }
    public String rejectCode() { return rejectCode; }

    // === 헬퍼 메서드 ===
    
    /**
     * 버퍼에서 고정 길이 문자열 읽기
     */
    private static String readStringField(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes).trim(); // 패딩된 공백 제거
    }

    /**
     * 버퍼에서 바이트 배열 읽기
     */
    private static byte[] readByteArray(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * 버퍼에 고정 길이 문자열 쓰기
     */
    private static void writeStringField(ByteBuffer buffer, String value, int length) {
        byte[] bytes = new byte[length];
        if (value != null) {
            byte[] valueBytes = value.getBytes();
            int copyLength = Math.min(valueBytes.length, length);
            System.arraycopy(valueBytes, 0, bytes, 0, copyLength);
        }
        // 나머지는 0으로 패딩됨 (배열 초기화 시 자동)
        buffer.put(bytes);
    }

    /**
     * 버퍼에 바이트 배열 쓰기
     */
    private static void writeByteArray(ByteBuffer buffer, byte[] value, int length) {
        byte[] bytes = new byte[length];
        if (value != null) {
            int copyLength = Math.min(value.length, length);
            System.arraycopy(value, 0, bytes, 0, copyLength);
        }
        buffer.put(bytes);
    }

    /**
     * 16진수 문자열로 변환 (디버깅용)
     */
    public String toHexString() {
        byte[] bytes = toBytes();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    /**
     * 상세한 문자열 표현 (디버깅 및 로깅용)
     */
    @Override
    public String toString() {
        return String.format(
            "ISO8583Header{" +
            "headerLength=%d, " +
            "flagAndVersion=0x%02X, " +
            "totalMsgLength=%d, " +
            "destId='%s', " +
            "srcId='%s', " +
            "reserved=%s, " +
            "batchNo=%d, " +
            "trxInfo='%s', " +
            "userInfo=0x%02X, " +
            "rejectCode='%s'" +
            "}",
            headerLength,
            headerFlagAndVersion & 0xFF,
            totalMessageLength,
            destinationId,
            sourceId,
            reserved != null ? Arrays.toString(reserved) : "null",
            batchNumber & 0xFF,
            transactionInfo,
            userInfo & 0xFF,
            rejectCode
        );
    }

    /**
     * Builder 클래스에 편의 메서드 추가
     */
    public static class ISO8583HeaderBuilder {
        
        /**
         * 표준 헤더 설정
         */
        public ISO8583HeaderBuilder withStandardDefaults() {
            return this
                .headerLength((byte) TOTAL_HEADER_SIZE)
                .headerFlagAndVersion((byte) 0x01) // 버전 1.0
                .reserved(new byte[RESERVED_SIZE])
                .batchNumber((byte) 0x01)
                .userInfo((byte) 0x00);
        }

        /**
         * UnionPay 특화 설정
         */
        public ISO8583HeaderBuilder withUnionPayDefaults(String destId, String srcId) {
            return withStandardDefaults()
                .destinationId(destId)
                .sourceId(srcId)
                .transactionInfo("UNIONPAY");
        }

        /**
         * Echo 메시지용 헤더
         */
        public ISO8583HeaderBuilder forEchoMessage(String destId, String srcId) {
            return withUnionPayDefaults(destId, srcId)
                .transactionInfo("ECHO");
        }

        /**
         * 결제 메시지용 헤더
         */
        public ISO8583HeaderBuilder forPaymentMessage(String destId, String srcId) {
            return withUnionPayDefaults(destId, srcId)
                .transactionInfo("PAYMENT");
        }
    }
}