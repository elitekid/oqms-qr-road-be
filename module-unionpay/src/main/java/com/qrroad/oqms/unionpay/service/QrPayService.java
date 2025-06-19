package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.unionpay.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class QrPayService {
//    private final OqUpiTokenRepository tokenRepository;
//
//    /**
//     * QR 결제 내역을 삽입합니다.
//     *
//     * @param map 삽입할 데이터가 포함된 맵
//     */
//    public void insertQrPayDtl(Map<String, Object> map) {
//    }
//
//    /**
//     * QR 결제 내역을 업데이트합니다.
//     *
//     * @param map 업데이트할 데이터가 포함된 맵
//     */
//    public void updateQrPayDtl(Map<String, Object> map) {
//    }
//
//    /**
//     * QR 결제 데이터를 생성합니다.
//     *
//     * @param dto 생성할 데이터가 포함된 DTO
//     * @param <T> DTO의 타입
//     */
//    public <T> void createQrPayData(T dto, String trxCl) {
//        if (dto instanceof TrxStatusNotificationUpiDto) {
//            createQrPayDataCpm((TrxStatusNotificationUpiDto) dto);
//        } else  if (dto instanceof MpqrcPaymentEmvRequestUpiDto) {
//            createQrPayDataMpmEmv((MpqrcPaymentEmvRequestUpiDto) dto, trxCl);
//        } else if (dto instanceof QrcInfoInquiryRequestUpiDto) {
//            createQrPayDataMpmBack((QrcInfoInquiryRequestUpiDto) dto, trxCl);
//        } else if (dto instanceof OrderVerifyRequestUpiDto) {
//            createQrPayDataMpmFront((OrderVerifyRequestUpiDto) dto, trxCl);
//        }
//    }
//
//    /**
//     * QR 결제 데이터를 업데이트합니다.
//     *
//     * @param dto 업데이트할 데이터가 포함된 DTO
//     * @param <T> DTO의 타입
//     */
//    public <T> void updateQrPayData(T dto) {
//        if (dto instanceof TrxStatusNotificationUpiDto) {
//            updateQrPayDataCpm((TrxStatusNotificationUpiDto) dto);
//        } else if (dto instanceof MpqrcPaymentEmvRequestUpiDto) {
//            updateQrPayDataMpmEmv((MpqrcPaymentEmvRequestUpiDto) dto);
//        } else if (dto instanceof MpqrcPaymentUrlRequestUpiDto) {
//            updateQrPayDataMpmUrl((MpqrcPaymentUrlRequestUpiDto) dto);
//        } else if (dto instanceof QrcInfoInquiryRequestUpiDto) {
//            updateQrPayDataMpmBack((QrcInfoInquiryRequestUpiDto) dto);
//        } else if (dto instanceof OrderVerifyRequestUpiDto) {
//            updateQrPayDataMpmFront((OrderVerifyRequestUpiDto) dto);
//        }
//    }
//
    /**
     * 페이로드에서 추가 데이터를 가져옵니다.
     *
     * @param payload 페이로드 문자열
     * @return 추가 데이터가 포함된 맵
     */
    public Map<String, String> getAdditionalDataInPayload(String payload) {
        Map<String, String> payloadMap = CommonUtil.parseTLV(payload);
        if (payloadMap.containsKey("62")) {
            return CommonUtil.parseTLV(payloadMap.get("62"));
        } else {
            return Collections.emptyMap();
        }
    }
//
//    /**
//     * CPM 방식의 QR 결제 데이터를 생성합니다.
//     *
//     * @param upiDto UPI DTO
//     */
//    private void createQrPayDataCpm(TrxStatusNotificationUpiDto upiDto) {
//        Map<String, Object> paramMap = CommonUtil.convertToMap(upiDto);
//
//        paramMap.put("trxDt", upiDto.getMsgInfo().getTimeStamp().substring(0, 8));
//        paramMap.put("trxTm", upiDto.getMsgInfo().getTimeStamp().substring(8));
//        paramMap.put("txnId", upiDto.getTrxInfo().getTxnID());
//        paramMap.put("instCd", "020");  // 우리은행
//        paramMap.put("trxIn", "00");     // CPM 거래
//
//        insertQrPayDtl(paramMap);
//    }
//
//    /**
//     * MPM EMV 방식의 QR 결제 데이터를 생성합니다.
//     *
//     * @param upiDto UPI DTO
//     */
//    private void createQrPayDataMpmEmv(MpqrcPaymentEmvRequestUpiDto upiDto, String trxCl) {
//        Map<String, Object> paramMap = CommonUtil.convertToMap(upiDto);
//
//        paramMap.put("trxCl", trxCl);
//        paramMap.put("trxDt", upiDto.getMsgInfo().getTimeStamp().substring(0, 8));
//        paramMap.put("trxTm", upiDto.getMsgInfo().getTimeStamp().substring(8));
//        paramMap.put("instCd", "020");  // 우리은행
//        paramMap.put("trxIn", "01");     // EMV 거래
//        paramMap.put("qrPayload", upiDto.getTrxInfo().getMpqrcPayload());
//
//        insertQrPayDtl(paramMap);
//    }
//
//    /**
//     * MPM Back-End 방식의 QR 결제 데이터를 생성합니다.
//     *
//     * @param upiDto UPI DTO
//     */
//    private void  createQrPayDataMpmBack(QrcInfoInquiryRequestUpiDto upiDto, String trxCl) {
//        Map<String, Object> paramMap = CommonUtil.convertToMap(upiDto);
//
//        paramMap.put("trxCl", trxCl);
//        paramMap.put("trxDt", upiDto.getMsgInfo().getTimeStamp().substring(0, 8));
//        paramMap.put("trxTm", upiDto.getMsgInfo().getTimeStamp().substring(8));
//        paramMap.put("instCd", "020");  // 우리은행
//        paramMap.put("trxIn", "02");     // MPM Back-End 거래
//        paramMap.put("token", tokenRepository.getToken(upiDto.getTrxInfo().getDeviceID()));
//        paramMap.put("qrPayload", upiDto.getTrxInfo().getMpqrcPayload());
//
//        insertQrPayDtl(paramMap);
//    }
//
//    /**
//     * MPM Back-Front 방식의 QR 결제 데이터를 생성합니다.
//     *
//     * @param upiDto UPI DTO
//     */
//    private void  createQrPayDataMpmFront(OrderVerifyRequestUpiDto upiDto, String trxCl) {
//        Map<String, Object> paramMap = CommonUtil.convertToMap(upiDto);
//
//        paramMap.put("trxCl", trxCl);
//        paramMap.put("trxDt", upiDto.getMsgInfo().getTimeStamp().substring(0, 8));
//        paramMap.put("trxTm", upiDto.getMsgInfo().getTimeStamp().substring(8));
//        paramMap.put("instCd", "020");  // 우리은행
//        paramMap.put("trxIn", "03");     // MPM Back-End 거래
//
//        insertQrPayDtl(paramMap);
//    }
//
//    /**
//     * CPM 방식의 QR 결제 데이터를 업데이트합니다.
//     *
//     * @param upiDto UPI DTO
//     */
//    private void updateQrPayDataCpm(TrxStatusNotificationUpiDto upiDto) {
//        Map<String, Object> paramMap = CommonUtil.convertToMap(upiDto);
//        paramMap.put("trxDt", upiDto.getMsgInfo().getTimeStamp().substring(0, 8));
//        paramMap.put("trxTm", upiDto.getMsgInfo().getTimeStamp().substring(8));
//
//        DiscountDetails[] discountDetails = Optional.ofNullable(upiDto.getTrxInfo().getDiscountDetails()).orElse(new DiscountDetails[0]);
//        for (int i = 0; i < discountDetails.length && i < 5; i++) {
//            paramMap.put("discountAmt" + (i + 1), discountDetails[i].getDiscountAmt());
//            paramMap.put("discountNote" + (i + 1), discountDetails[i].getDiscountNote());
//        }
//
//        updateQrPayDtl(paramMap);
//    }
//
//    /**
//     * MPM EMV 방식의 QR 결제 데이터를 업데이트합니다.
//     *
//     * @param upiDto UPI DTO
//     */
//    private void updateQrPayDataMpmEmv(MpqrcPaymentEmvRequestUpiDto upiDto) {
//        Map<String, Object> paramMap = CommonUtil.convertToMap(upiDto);
//        paramMap.put("trxDt", upiDto.getMsgInfo().getTimeStamp().substring(0, 8));
//        paramMap.put("trxTm", upiDto.getMsgInfo().getTimeStamp().substring(8));
//        paramMap.put("qrPayload", upiDto.getTrxInfo().getMpqrcPayload());
//
//        DiscountDetails[] discountDetails = Optional.ofNullable(upiDto.getTrxInfo().getDiscountDetails()).orElse(new DiscountDetails[0]);
//        for (int i = 0; i < discountDetails.length && i < 5; i++) {
//            paramMap.put("discountAmt" + (i + 1), discountDetails[i].getDiscountAmt());
//            paramMap.put("discountNote" + (i + 1), discountDetails[i].getDiscountNote());
//        }
//
//        paramMap.put("responseCode", upiDto.getMsgResponse().getResponseCode());
//        paramMap.put("responseMsg", upiDto.getMsgResponse().getResponseMsg());
//        paramMap.put("paymentStatus", "APPROVED");
//
//        updateQrPayDtl(paramMap);
//    }
//
//    /**
//     * MPM URL 방식의 QR 결제 데이터를 업데이트합니다. (최종 결제결과 업데이트)
//     *
//     * @param upiDto UPI DTO
//     */
//    private void updateQrPayDataMpmUrl(MpqrcPaymentUrlRequestUpiDto upiDto) {
//        Map<String, Object> paramMap = CommonUtil.convertToMap(upiDto);
//        paramMap.put("qrPayload", upiDto.getTrxInfo().getMpqrcPayload());
//        paramMap.put("paymentStatus", "APPROVED");
//
//        DiscountDetails[] discountDetails = Optional.ofNullable(upiDto.getTrxInfo().getDiscountDetails()).orElse(new DiscountDetails[0]);
//        for (int i = 0; i < discountDetails.length && i < 5; i++) {
//            paramMap.put("discountAmt" + (i + 1), discountDetails[i].getDiscountAmt());
//            paramMap.put("discountNote" + (i + 1), discountDetails[i].getDiscountNote());
//        }
//
//        updateQrPayDtl(paramMap);
//    }
//
//    /**
//     * MPM Back-End 방식의 QR 결제 데이터를 업데이트합니다.
//     *
//     * @param upiDto UPI DTO
//     */
//    private void updateQrPayDataMpmBack(QrcInfoInquiryRequestUpiDto upiDto) {
//        Map<String, Object> paramMap = CommonUtil.convertToMap(upiDto);
//        paramMap.put("trxDt", upiDto.getMsgInfo().getTimeStamp().substring(0, 8));
//        paramMap.put("trxTm", upiDto.getMsgInfo().getTimeStamp().substring(8));
//        paramMap.put("qrPayload", upiDto.getTrxInfo().getMpqrcPayload());
//
//        updateQrPayDtl(paramMap);
//    }
//
//    /**
//     * MPM Back-Front 방식의 QR 결제 데이터를 업데이트합니다.
//     *
//     * @param upiDto UPI DTO
//     */
//    private void updateQrPayDataMpmFront(OrderVerifyRequestUpiDto upiDto) {
//        Map<String, Object> paramMap = CommonUtil.convertToMap(upiDto);
//        paramMap.put("trxDt", upiDto.getMsgInfo().getTimeStamp().substring(0, 8));
//        paramMap.put("trxTm", upiDto.getMsgInfo().getTimeStamp().substring(8));
//
//        DiscountDetails[] discountDetails = Optional.ofNullable(upiDto.getTrxInfo().getDiscountDetails()).orElse(new DiscountDetails[0]);
//        for (int i = 0; i < discountDetails.length && i < 5; i++) {
//            paramMap.put("discountAmt" + (i + 1), discountDetails[i].getDiscountAmt());
//            paramMap.put("discountNote" + (i + 1), discountDetails[i].getDiscountNote());
//        }
//        updateQrPayDtl(paramMap);
//    }

}
