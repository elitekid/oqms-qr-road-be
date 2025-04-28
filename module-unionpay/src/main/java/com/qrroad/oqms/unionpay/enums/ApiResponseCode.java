package com.qrroad.oqms.unionpay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiResponseCode {
    
//UPI 표준응답	
	// 승인됨
    APPROVED("00", "Approved"),
    // 카드 발급사에 문의하세요
    REFER_TO_CARD_ISSUER("01", "Please refer to the card issuer"),
    // QR 코드를 업데이트하세요
    UPDATE_QR_CODE("02", "Please update the QR code"),
    // 유효하지 않은 가맹점
    INVALID_MERCHANT("03", "Invalid Merchant"),
    // 보류 중. 거래 결과를 알 수 없습니다. 나중에 다시 확인하세요.
    PENDING("04", "Pending. Transaction result is unknown. Please check later."),
    // 카드 소유자 인증 실패
    CARDHOLDER_VERIFICATION_FAILS("05", "Cardholder verification fails"),
    // TSP 오류
    TSP_ERROR("08", "TSP error"),
    // 유효하지 않은 거래
    INVALID_TRANSACTION("12", "Invalid transaction"),
    // 유효하지 않은 금액
    INVALID_AMOUNT("13", "Invalid amount"),
    // 유효하지 않은 카드 번호
    INVALID_CARD_NUMBER("14", "Invalid card number"),
    // 해당 발급사 없음
    NO_SUCH_ISSUER("15", "No such Issuer"),
    // 카드 상태 오류
    CARD_STATUS_ERROR("21", "Card status error"),
    // 원 거래를 찾을 수 없음
    UNABLE_TO_LOCATE_ORIGINAL_TRANSACTION("25", "Unable to locate the original transaction"),
    // 메시지 형식 오류
    MESSAGE_FORMAT_ERROR("30", "Message format error"),
    // OTP/PIN 최대 시도 횟수 초과
    EXCEED_OTP_PIN_MAX_TRIES("32", "Exceed OTP/PIN max tries"),
    // 사기 카드
    FRAUD_CARD("34", "Fraud card"),
    // 거래가 UnionPay 리스크 관리 시스템에 의해 거부되었습니다. 전화번호 '95516'으로 UPI에 문의하세요.
    TRANSACTION_REJECTED_BY_RISK_MANAGEMENT("38", "The transaction is rejected by UnionPay risk management system. Please contact UPI via phone number '95516'."),
    // 발급사가 지원하지 않는 거래
    TRANSACTION_NOT_SUPPORTED_BY_ISSUER("40", "The transaction is not supported by the Issuer"),
    // 분실 카드
    LOST_CARD("41", "Lost card"),
    // 도난 카드
    STOLEN_CARD("43", "Stolen card"),
    // 잔액 부족
    INSUFFICIENT_BALANCE("51", "Insufficient balance"),
    // 만료된 카드
    EXPIRED_CARD("54", "Expired card"),
    // 유효하지 않은 장치 ID
    INVALID_DEVICE_ID("55", "Invalid device id"),
    // 카드 소유자에게 허용되지 않는 거래
    TRANSACTION_NOT_PERMITTED_TO_CARDHOLDER("57", "Transaction not permitted to Cardholder"),
    // 승인 금액 한도 초과
    EXCEEDS_APPROVAL_AMOUNT_LIMIT("61", "Exceeds approval amount limit"),
    // 제한된 거래
    RESTRICTED_TRANSACTION("62", "Restricted transaction"),
    // 발급사의 유효성 검사 오류
    VALIDATION_ERROR_FROM_ISSUER("70", "Validation error from Issuer"),
    // 발급사 거부
    ISSUER_DECLINED("71", "Issuer declined"),
    // 발급사 MAC 검증 실패
    ISSUER_VERIFY_MAC_FAILED("72", "Issuer verify mac failed"),
    // 등록되지 않음
    ENROLLMENT_NOT_FOUND("73", "Enrollment not found"),
    // OTP 만료
    OTP_EXPIRED("74", "OTP expired"),
    // 유효하지 않은 OTP
    INVALID_OTP("75", "Invalid OTP"),
    // OTP를 찾을 수 없음
    OTP_NOT_FOUND("76", "OTP not found"),
    // IDV 설정되지 않음
    IDV_NOT_SET("77", "IDV not set"),
    // 중복 요청
    DUPLICATE_REQUEST("78", "Duplicate request"),
    // 프로모션 규칙을 충족하지 않음
    NOT_FULFILL_PROMOTION_RULES("85", "Not fulfill promotion rules"),
    // 카드가 이미 발급됨
    CARD_ALREADY_PROVISIONED("88", "Card already provisioned"),
    // 카드 프로필을 찾을 수 없음
    CARD_PROFILE_NOT_FOUND("89", "Card profile not found"),
    // 시스템이 차단됨
    SYSTEM_IN_CUT_OFF("90", "The system is in cut-off"),
    // 발급사 시스템 오류
    ISSUER_SYSTEM_ERROR("91", "Issuer system error"),
    // 네트워크 오류
    NETWORK_ERROR("92", "Network error"),
    // 유효하지 않은 등록 상태
    INVALID_ENROLL_STATE("93", "Invalid enroll state"),
    // 중복 거래
    DUPLICATED_TRANSACTION("94", "Duplicated transaction"),
    // 등록 시간 초과
    ENROLMENT_TIMED_OUT("95", "Enrolment timed out"),
    // UnionPay 시스템 오류
    UNIONPAY_SYSTEM_ERROR("96", "UnionPay system error"),
    // 시간 초과
    TIMEOUT("98", "Timeout"),
    // 기타 오류
    OTHER_ERROR("99", "Other error")
    
    ;
	
    private final String code;
    private final String message;
}