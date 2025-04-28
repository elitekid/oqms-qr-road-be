package com.qrroad.oqms.core.enums;

import lombok.Getter;

@Getter
public enum InstitutionCode {
    KOREA_BANK("001", "한국은행"),
    INDUSTRIAL_BANK("002", "산업은행"),
    IBK("003", "가상계좌 채번가능 기업은행"),
    KB_BANK("004", "가상계좌 채번가능 국민은행"),
    FOREIGN_EXCHANGE_BANK("005", "외환은행"),
    SUHYUP_BANK("007", "수협은행"),
    EXIM_BANK("008", "수출입은행"),
    NH_BANK("011", "가상계좌 채번가능 농협은행"),
    NH_MEMBERSHIP("012", "농협회원조합"),
    WOORI_BANK("020", "가상계좌 채번가능 우리은행"),
    SC_FIRST_BANK("023", "가상계좌 채번가능 SC제일은행"),
    SEOUL_BANK("026", "서울은행"),
    CITI_BANK("027", "한국씨티은행"),
    IM_BANK("031", "가상계좌 채번가능 iM뱅크(대구)"),
    BUSAN_BANK("032", "가상계좌 채번가능 부산은행"),
    GWANGJU_BANK("034", "가상계좌 채번가능 광주은행"),
    JEJU_BANK("035", "제주은행"),
    JEONBUK_BANK("037", "전북은행"),
    KYONGNAM_BANK("039", "경남은행"),
    SAEMAUL("045", "새마을금고연합회"),
    CREDIT_UNION("048", "신협중앙회"),
    SAVINGS_BANK("050", "상호저축은행"),
    FOREIGN_BANK_OTHERS("051", "기타 외국계은행"),
    MORGAN_STANLEY("052", "모건스탠리은행"),
    HSBC("054", "HSBC은행"),
    DEUTSCHE_BANK("055", "도이치은행"),
    RBS_BANK("056", "알비에스피엘씨은행"),
    JP_MORGAN("057", "제이피모간체이스은행"),
    MIZUHO_CORPORATE_BANK("058", "미즈호코퍼레이트은행"),
    MUFG("059", "미쓰비시도쿄UFJ은행"),
    BOA("060", "BOA"),
    BNP_PARIBAS("061", "비엔피파리바은행"),
    ICBC("062", "중국공상은행"),
    BANK_OF_CHINA("063", "중국은행"),
    FORESTRY_COOPERATIVE("064", "산림조합"),
    DAEHWA_BANK("065", "대화은행"),
    KOREA_POST("071", "가상계좌 채번가능 우체국"),
    CREDIT_GUARANTEE_FUND("076", "신용보증기금"),
    TECHNOLOGY_CREDIT_GUARANTEE_FUND("077", "기술신용보증기금"),
    HANA_BANK("081", "가상계좌 채번가능 하나은행"),
    SHINHAN_BANK("088", "가상계좌 채번가능 신한은행"),
    K_BANK("089", "가상계좌 채번가능 케이뱅크"),
    KAKAO_BANK("090", "카카오뱅크"),
    TOSS_BANK("092", "토스뱅크"),
    HOUSING_FINANCE("093", "한국주택금융공사"),
    SEOUL_GUARANTEE_INSURANCE("094", "서울보증보험"),
    NATIONAL_POLICE_AGENCY("095", "경찰청"),
    KFTC("099", "금융결제원"),
    DONGYANG_SECURITIES("209", "동양종합금융증권"),
    HYUNDAI_SECURITIES("218", "현대증권"),
    MIRAEOASSET_SECURITIES("230", "미래에셋증권"),
    DAEWOO_SECURITIES("238", "대우증권"),
    SAMSUNG_SECURITIES("240", "삼성증권"),
    KOREA_INVESTMENT_SECURITIES("243", "한국투자증권"),
    NH_INVESTMENT_SECURITIES("247", "NH투자증권"),
    KYOBO_SECURITIES("261", "교보증권"),
    HI_INVESTMENT_SECURITIES("262", "하이투자증권"),
    HMC_SECURITIES("263", "에이치엠씨투자증권"),
    KIWOOM_SECURITIES("264", "키움증권"),
    ET_SECUITIES("265", "이트레이드증권"),
    SK_SECURITIES("266", "SK증권"),
    DAE_SHIN_SECURITIES("267", "대신증권"),
    SOLOMON_SECURITIES("268", "솔로몬투자증권"),
    HANWHA_SECURITIES("269", "한화증권"),
    HANA_DAETOO_SECURITIES("270", "하나대투증권"),
    TOSS_SECURITIES("271", "토스증권"),
    SHINHAN_INVESTMENT("278", "신한금융투자"),
    DONG_BU_SECURITIES("279", "동부증권"),
    EUGENE_INVESTMENT("280", "유진투자증권"),
    MERITZ_SECURITIES("287", "메리츠증권"),
    NH_INVESTMENT("289", "엔에이치투자증권"),
    BOOKOOK_SECURITIES("290", "부국증권"),
    SHINYOUNG_SECURITIES("291", "신영증권"),
    LIG_INVESTMENT("292", "엘아이지투자증권");

    private final String code;
    private final String name;

    InstitutionCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Code: %s, Name: %s", code, name);
    }
}
