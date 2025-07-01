package com.qrroad.oqms.unionpay.dto.iso8583;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * ISO8583 전문 바디 섹션 
 * 
 * Builder 패턴을 적용하여 가독성과 유연성을 향상시킨 ISO8583 메시지 DTO
 * - 선택적 필드 설정의 편의성 제공
 * - 불변 객체로 설계하여 Thread-safety 보장
 * - Method chaining을 통한 직관적인 객체 생성
 * 
 * @author escapetree82
 * @version 2.0
 * @since 2025.07.01
 */
@Builder(toBuilder = true)
@Slf4j
public class ISO8583Dto {

    // === 기본 메시지 정보 ===
    
    /**
     * F0: Message Type Indicator
     * 메시지 유형을 나타내는 4자리 숫자
     * 예: 0200(결제요청), 0210(결제응답), 0800(네트워크관리요청), 0810(네트워크관리응답)
     */
    private final String mti;

    /**
     * F1: Primary Bit Map
     * 존재하는 필드를 나타내는 비트맵 (8바이트 또는 16바이트)
     */
    private final String primaryBitMap;

    // === 거래 관련 필드 ===
    
    /**
     * F2: Primary Account Number (PAN)
     * 카드번호 (최대 19자리)
     */
    private final String pan;

    /**
     * F3: Processing Code
     * 거래 처리 코드 (6자리)
     * 예: 000000(구매), 200000(환불), 900000(조회)
     */
    private final String procCd;

    /**
     * F4: Amount, Transaction
     * 거래 금액 (12자리, 센트 단위)
     */
    private final String trAmt;

    /**
     * F5: Amount, Settlement
     * 정산 금액 (12자리)
     */
    private final String settleAmt;

    /**
     * F6: Amount, Cardholder Billing
     * 카드소지자 청구 금액 (12자리)
     */
    private final String billAmt;

    // === 시간 관련 필드 ===
    
    /**
     * F7: Transmission Date and Time
     * 전송 일시 (MMDDhhmmss 형식, 10자리)
     */
    private final String trDateTime;

    /**
     * F11: System Trace Audit Number (STAN)
     * 시스템 추적 감사 번호 (6자리)
     */
    private final String stan;

    /**
     * F12: Time, Local Transaction
     * 현지 거래 시간 (hhmmss 형식, 6자리)
     */
    private final String localTime;

    /**
     * F13: Date, Local Transaction
     * 현지 거래 날짜 (MMDD 형식, 4자리)
     */
    private final String localDate;

    /**
     * F14: Date, Expiration
     * 만료일 (YYMM 형식, 4자리)
     */
    private final String expDate;

    /**
     * F15: Date, Settlement
     * 정산일 (MMDD 형식, 4자리)
     */
    private final String settleDate;

    /**
     * F16: Date, Conversion
     * 환율 적용일 (MMDD 형식, 4자리)
     */
    private final String convDate;

    // === 환율 관련 필드 ===
    
    /**
     * F9: Conversion Rate, Settlement
     * 정산 환율 (8자리)
     */
    private final String convRateSettle;

    /**
     * F10: Conversion Rate, Cardholder Billing
     * 카드소지자 청구 환율 (8자리)
     */
    private final String convRateBill;

    // === 머천트 관련 필드 ===
    
    /**
     * F18: Merchant's Type
     * 가맹점 유형 코드 (4자리)
     */
    private final String merchantType;

    /**
     * F19: Merchant Country Code
     * 가맹점 국가 코드 (3자리)
     */
    private final String merchantCountry;

    /**
     * F41: Card Acceptor Terminal Identification
     * 단말기 ID (8자리)
     */
    private final String termId;

    /**
     * F42: Card Acceptor Identification Code
     * 가맹점 ID (15자리)
     */
    private final String acceptorId;

    /**
     * F43: Card Acceptor Name/Location
     * 가맹점명/위치 (40자리)
     */
    private final String acceptorNameLoc;

    // === POS 관련 필드 ===
    
    /**
     * F22: Point of Service Entry Mode Code
     * POS 입력 모드 코드 (3자리)
     */
    private final String posEntryMode;

    /**
     * F23: Card Sequence Number
     * 카드 일련번호 (3자리)
     */
    private final String cardSeqNo;

    /**
     * F25: Point of Service Condition Code
     * POS 조건 코드 (2자리)
     */
    private final String posCondCode;

    /**
     * F26: Point of Service PIN Capture Code
     * POS PIN 캡처 코드 (2자리)
     */
    private final String posPinCode;

    // === 수수료 및 금액 관련 ===
    
    /**
     * F28: Amount, Transaction Fee
     * 거래 수수료 (8자리)
     */
    private final String transFeeAmount;

    /**
     * F54: Additional Amounts
     * 추가 금액 정보
     */
    private final String addlAmts;

    // === 기관 코드 ===
    
    /**
     * F32: Acquiring Institution Identification Code
     * 매입기관 식별코드 (11자리 변동길이)
     */
    private final String acqInstCode;

    /**
     * F33: Forwarding Institution Identification Code
     * 전달기관 식별코드 (11자리 변동길이)
     */
    private final String fwdInstCode;

    /**
     * F100: Receiving Institution Identification Code
     * 수신기관 식별코드 (11자리 변동길이)
     */
    private final String recvInstCode;

    // === 트랙 데이터 ===
    
    /**
     * F35: Track 2 Data
     * 트랙2 데이터 (37자리 변동길이)
     */
    private final String track2Data;

    /**
     * F36: Track 3 Data
     * 트랙3 데이터 (104자리 변동길이)
     */
    private final String track3Data;

    /**
     * F45: Track 1 Data
     * 트랙1 데이터 (76자리 변동길이)
     */
    private final String track1Data;

    // === 참조 번호 및 응답 코드 ===
    
    /**
     * F37: Retrieval Reference Number (RRN)
     * 참조번호 (12자리)
     */
    private final String rrn;

    /**
     * F38: Authorization Identification Response
     * 승인번호 (6자리)
     */
    private final String authRespCode;

    /**
     * F39: Response Code
     * 응답코드 (2자리)
     * 예: 00(승인), 05(거절), 14(유효하지않은카드번호)
     */
    private final String respCode;

    // === 추가 데이터 ===
    
    /**
     * F44: Additional Response Data
     * 추가 응답 데이터 (25자리 변동길이)
     */
    private final String addlRespData;

    /**
     * F48: Additional Data-Private
     * 추가 데이터-사설 (512자리 변동길이)
     */
    private final String addlDataPriv;

    // === 통화 코드 ===
    
    /**
     * F49: Currency Code, Transaction
     * 거래 통화코드 (3자리)
     * 예: 410(KRW), 840(USD), 392(JPY)
     */
    private final String currCodeTrans;

    /**
     * F50: Currency Code, Settlement
     * 정산 통화코드 (3자리)
     */
    private final String currCodeSettle;

    /**
     * F51: Currency Code, Cardholder Billing
     * 카드소지자 청구 통화코드 (3자리)
     */
    private final String currCodeBill;

    // === 보안 관련 ===
    
    /**
     * F52: PIN Data
     * PIN 데이터 (8자리)
     */
    private final String pinData;

    /**
     * F53: Security Related Control Information
     * 보안 관련 제어 정보 (16자리)
     */
    private final String secControlInfo;

    /**
     * F96: Message Security Code
     * 메시지 보안 코드 (8바이트 바이너리)
     */
    private final String msgSecCode;

    /**
     * F128: Message Authentication Code (MAC)
     * 메시지 인증 코드 (8바이트 바이너리)
     */
    private final String mac;

    // === IC 카드 및 토큰 ===
    
    /**
     * F55: IC Card Data
     * IC카드 데이터 (255자리 변동길이)
     */
    private final String icCardData;

    /**
     * F56: Token Payment Account Reference
     * 토큰 결제 계정 참조 (512자리 변동길이)
     */
    private final String tokenRef;

    // === 계정 정보 ===
    
    /**
     * F102: Account Identification 1
     * 계정 식별 1 (28자리 변동길이)
     */
    private final String acctId1;

    /**
     * F103: Account Identification 2
     * 계정 식별 2 (28자리 변동길이)
     */
    private final String acctId2;

    // === 업종 및 거래 정보 ===
    
    /**
     * F104: Transaction and Industry Application Information
     * 거래 및 업종 응용 정보 (512자리 변동길이)
     */
    private final String transIndInfo;

    // === 추가 정보 ===
    
    /**
     * F57: Issuer Additional Data - Private
     * 발급기관 추가 데이터-사설 (100자리 변동길이)
     */
    private final String issuerAddlData;

    /**
     * F60: Self-Defined Field
     * 자체 정의 필드 (100자리 변동길이)
     */
    private final String selfDefinedField;

    /**
     * F61: Cardholder Authentication Information
     * 카드소지자 인증 정보 (200자리 변동길이)
     */
    private final String cardholderAuth;

    /**
     * F62: Switching Data (Not used)
     * 스위칭 데이터 (200자리 변동길이) - 사용안함
     */
    private final String switchData;

    /**
     * F63: Financial Network Data
     * 금융 네트워크 데이터 (512자리 변동길이)
     */
    private final String finNetData;

    /**
     * F113: Additional Information
     * 추가 정보 (512자리 변동길이)
     */
    private final String addlInfo;

    // === 네트워크 관리 ===
    
    /**
     * F70: Network Management Information Code
     * 네트워크 관리 정보 코드 (3자리)
     * 예: 001(Echo test), 301(Sign-on), 302(Sign-off)
     */
    private final String networkInfoCode;

    /**
     * F90: Original Data
     * 원거래 데이터 (42자리)
     * 원거래의 MTI + STAN + 전송일시 + 매입기관코드
     */
    private final String origData;

    // === 지역 및 예약 필드 ===
    
    /**
     * F117: National and Regional Information
     * 국가 및 지역 정보 (256자리 변동길이)
     */
    private final String nationalInfo;

    /**
     * F121: GSCS Reserved
     * GSCS 예약 필드 (100자리 변동길이)
     */
    private final String gscsReserved;

    /**
     * F122: Acquirer Institution Reserved
     * 매입기관 예약 필드 (100자리 변동길이)
     */
    private final String acqReserved;

    /**
     * F123: Issuer Institution Reserved
     * 발급기관 예약 필드 (100자리 변동길이)
     */
    private final String issuerReserved;

    /**
     * F125: Security and Risk Assessment Information
     * 보안 및 위험도 평가 정보 (256자리 변동길이)
     */
    private final String secRiskInfo;

    // === 헤더 정보 ===
    
    /**
     * ISO8583 헤더 정보
     */
    private final ISO8583Header header;

    // === 접근자 메서드 (모든 필드) ===
    
    public String mti() { return mti; }
    public String primaryBitMap() { return primaryBitMap; }
    public String pan() { return pan; }
    public String procCd() { return procCd; }
    public String trAmt() { return trAmt; }
    public String settleAmt() { return settleAmt; }
    public String billAmt() { return billAmt; }
    public String trDateTime() { return trDateTime; }
    public String stan() { return stan; }
    public String localTime() { return localTime; }
    public String localDate() { return localDate; }
    public String expDate() { return expDate; }
    public String settleDate() { return settleDate; }
    public String convDate() { return convDate; }
    public String convRateSettle() { return convRateSettle; }
    public String convRateBill() { return convRateBill; }
    public String merchantType() { return merchantType; }
    public String merchantCountry() { return merchantCountry; }
    public String termId() { return termId; }
    public String acceptorId() { return acceptorId; }
    public String acceptorNameLoc() { return acceptorNameLoc; }
    public String posEntryMode() { return posEntryMode; }
    public String cardSeqNo() { return cardSeqNo; }
    public String posCondCode() { return posCondCode; }
    public String posPinCode() { return posPinCode; }
    public String transFeeAmount() { return transFeeAmount; }
    public String addlAmts() { return addlAmts; }
    public String acqInstCode() { return acqInstCode; }
    public String fwdInstCode() { return fwdInstCode; }
    public String recvInstCode() { return recvInstCode; }
    public String track2Data() { return track2Data; }
    public String track3Data() { return track3Data; }
    public String track1Data() { return track1Data; }
    public String rrn() { return rrn; }
    public String authRespCode() { return authRespCode; }
    public String respCode() { return respCode; }
    public String addlRespData() { return addlRespData; }
    public String addlDataPriv() { return addlDataPriv; }
    public String currCodeTrans() { return currCodeTrans; }
    public String currCodeSettle() { return currCodeSettle; }
    public String currCodeBill() { return currCodeBill; }
    public String pinData() { return pinData; }
    public String secControlInfo() { return secControlInfo; }
    public String msgSecCode() { return msgSecCode; }
    public String mac() { return mac; }
    public String icCardData() { return icCardData; }
    public String tokenRef() { return tokenRef; }
    public String acctId1() { return acctId1; }
    public String acctId2() { return acctId2; }
    public String transIndInfo() { return transIndInfo; }
    public String issuerAddlData() { return issuerAddlData; }
    public String selfDefinedField() { return selfDefinedField; }
    public String cardholderAuth() { return cardholderAuth; }
    public String switchData() { return switchData; }
    public String finNetData() { return finNetData; }
    public String addlInfo() { return addlInfo; }
    public String networkInfoCode() { return networkInfoCode; }
    public String origData() { return origData; }
    public String nationalInfo() { return nationalInfo; }
    public String gscsReserved() { return gscsReserved; }
    public String acqReserved() { return acqReserved; }
    public String issuerReserved() { return issuerReserved; }
    public String secRiskInfo() { return secRiskInfo; }
    public ISO8583Header header() { return header; }

    // === 편의 메서드 ===
    
    public String maskedPan() { return maskPan(pan); }
    
    public boolean isEchoRequest() { return "0800".equals(mti); }
    public boolean isEchoResponse() { return "0810".equals(mti); }
    public boolean isPaymentRequest() { return "0200".equals(mti); }
    public boolean isPaymentResponse() { return "0210".equals(mti); }
    public boolean isReversalRequest() { return "0400".equals(mti); }
    public boolean isReversalResponse() { return "0410".equals(mti); }
    
    public boolean isApproved() { return "00".equals(respCode); }
    public boolean isDeclined() { return respCode != null && !"00".equals(respCode); }
    
    public boolean hasRequiredFields() { return mti != null && stan != null && trDateTime != null; }

    /**
     * 디버깅 및 로깅을 위한 문자열 표현
     * 민감한 정보(PAN, PIN 등)는 마스킹하여 출력
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // 헤더 정보
        if (header != null) {
            sb.append(header.toString()).append("\n");
        }
        
        // 필드 정보 (민감한 데이터는 마스킹)
        sb.append(formatField("F0", "MTI", mti))
          .append(formatField("F2", "PAN", maskPan(pan)))
          .append(formatField("F3", "Processing Code", procCd))
          .append(formatField("F4", "Transaction Amount", trAmt))
          .append(formatField("F5", "Settlement Amount", settleAmt))
          .append(formatField("F6", "Billing Amount", billAmt))
          .append(formatField("F7", "Transmission Date and Time", trDateTime))
          .append(formatField("F9", "Conversion Rate Settlement", convRateSettle))
          .append(formatField("F10", "Conversion Rate Billing", convRateBill))
          .append(formatField("F11", "System Trace Audit Number", stan))
          .append(formatField("F12", "Local Transaction Time", localTime))
          .append(formatField("F13", "Local Transaction Date", localDate))
          .append(formatField("F14", "Expiration Date", expDate))
          .append(formatField("F15", "Settlement Date", settleDate))
          .append(formatField("F16", "Conversion Date", convDate))
          .append(formatField("F18", "Merchant Type", merchantType))
          .append(formatField("F19", "Merchant Country", merchantCountry))
          .append(formatField("F22", "Point of Service Entry Mode", posEntryMode))
          .append(formatField("F23", "Card Sequence Number", cardSeqNo))
          .append(formatField("F25", "Point of Service Condition Code", posCondCode))
          .append(formatField("F26", "Point of Service PIN Capture Code", posPinCode))
          .append(formatField("F28", "Transaction Fee Amount", transFeeAmount))
          .append(formatField("F32", "Acquiring Institution Code", acqInstCode))
          .append(formatField("F33", "Forwarding Institution Code", fwdInstCode))
          .append(formatField("F35", "Track 2 Data", maskTrackData(track2Data)))
          .append(formatField("F36", "Track 3 Data", maskTrackData(track3Data)))
          .append(formatField("F37", "Retrieval Reference Number", rrn))
          .append(formatField("F38", "Authorization Response Code", authRespCode))
          .append(formatField("F39", "Response Code", respCode))
          .append(formatField("F41", "Terminal ID", termId))
          .append(formatField("F42", "Acceptor ID", acceptorId))
          .append(formatField("F43", "Acceptor Name/Location", acceptorNameLoc))
          .append(formatField("F44", "Additional Response Data", addlRespData))
          .append(formatField("F45", "Track 1 Data", maskTrackData(track1Data)))
          .append(formatField("F48", "Additional Data - Private", addlDataPriv))
          .append(formatField("F49", "Currency Code - Transaction", currCodeTrans))
          .append(formatField("F50", "Currency Code - Settlement", currCodeSettle))
          .append(formatField("F51", "Currency Code - Billing", currCodeBill))
          .append(formatField("F52", "PIN Data", maskPinData(pinData)))
          .append(formatField("F53", "Security Control Info", secControlInfo))
          .append(formatField("F54", "Additional Amounts", addlAmts))
          .append(formatField("F55", "IC Card Data", icCardData))
          .append(formatField("F56", "Token Ref", tokenRef))
          .append(formatField("F57", "Issuer Additional Data", issuerAddlData))
          .append(formatField("F60", "Self-Defined Field", selfDefinedField))
          .append(formatField("F61", "Cardholder Auth", cardholderAuth))
          .append(formatField("F62", "Switch Data", switchData))
          .append(formatField("F63", "Financial Network Data", finNetData))
          .append(formatField("F70", "Network Info Code", networkInfoCode))
          .append(formatField("F90", "Original Data", origData))
          .append(formatField("F96", "Message Security Code", msgSecCode))
          .append(formatField("F100", "Receiving Institution Code", recvInstCode))
          .append(formatField("F102", "Account ID 1", acctId1))
          .append(formatField("F103", "Account ID 2", acctId2))
          .append(formatField("F104", "Transaction and Industry Info", transIndInfo))
          .append(formatField("F113", "Additional Info", addlInfo))
          .append(formatField("F117", "National Info", nationalInfo))
          .append(formatField("F121", "GSCS Reserved", gscsReserved))
          .append(formatField("F122", "Acquirer Reserved", acqReserved))
          .append(formatField("F123", "Issuer Reserved", issuerReserved))
          .append(formatField("F125", "Security Risk Info", secRiskInfo))
          .append(formatField("F128", "Message Authentication Code", maskPinData(mac)));
        
        return sb.toString();
    }

    /**
     * 필드 포맷팅 헬퍼 메서드
     */
    private String formatField(String fieldCode, String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return String.format("%s %-35s: %s%n", fieldCode, fieldName, value);
    }

    /**
     * PAN 마스킹 (앞 6자리, 뒤 4자리만 표시)
     */
    private String maskPan(String pan) {
        if (pan == null || pan.length() < 10) {
            return pan;
        }
        return pan.substring(0, 6) + "*".repeat(pan.length() - 10) + pan.substring(pan.length() - 4);
    }

    /**
     * 트랙 데이터 마스킹
     */
    private String maskTrackData(String trackData) {
        if (trackData == null || trackData.length() < 8) {
            return trackData;
        }
        return trackData.substring(0, 4) + "*".repeat(trackData.length() - 8) + trackData.substring(trackData.length() - 4);
    }

    /**
     * PIN 데이터 마스킹
     */
    private String maskPinData(String pinData) {
        return pinData != null ? "*".repeat(pinData.length()) : null;
    }

    /**
     * 빌더 클래스에 편의 메서드 추가
     */
    public static class ISO8583DtoBuilder {
        
        /**
         * Echo 테스트 메시지 빌더
         */
        public ISO8583DtoBuilder echoRequest() {
            return this.mti("0800").networkInfoCode("001");
        }

        /**
         * Echo 응답 메시지 빌더
         */
        public ISO8583DtoBuilder echoResponse() {
            return this.mti("0810").networkInfoCode("001").respCode("00");
        }

        /**
         * 결제 요청 메시지 빌더
         */
        public ISO8583DtoBuilder paymentRequest() {
            return this.mti("0200").procCd("000000");
        }

        /**
         * 결제 응답 메시지 빌더
         */
        public ISO8583DtoBuilder paymentResponse() {
            return this.mti("0210").procCd("000000");
        }

        /**
         * 취소 요청 메시지 빌더
         */
        public ISO8583DtoBuilder reversalRequest() {
            return this.mti("0400").procCd("000000");
        }

        /**
         * 취소 응답 메시지 빌더
         */
        public ISO8583DtoBuilder reversalResponse() {
            return this.mti("0410").procCd("000000");
        }

        /**
         * 현재 시간 정보로 시간 필드들을 자동 설정
         */
        public ISO8583DtoBuilder withCurrentDateTime() {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            return this
                .trDateTime(now.format(java.time.format.DateTimeFormatter.ofPattern("MMddHHmmss")))
                .localTime(now.format(java.time.format.DateTimeFormatter.ofPattern("HHmmss")))
                .localDate(now.format(java.time.format.DateTimeFormatter.ofPattern("MMdd")));
        }

        /**
         * STAN 자동 생성 (간단한 구현 - 실제로는 별도 관리 필요)
         */
        public ISO8583DtoBuilder withAutoStan() {
            String stan = String.format("%06d", System.currentTimeMillis() % 1000000);
            return this.stan(stan);
        }

        /**
         * 기본 한국 설정 적용
         */
        public ISO8583DtoBuilder withKoreanDefaults() {
            return this
                .currCodeTrans("410")  // KRW
                .merchantCountry("410"); // Korea
        }
    }
}