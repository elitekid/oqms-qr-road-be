package com.qrroad.oqms.unionpay.controller;

import com.qrroad.oqms.unionpay.dto.ApiMsgDto;
import com.qrroad.oqms.unionpay.service.*;
import com.qrroad.oqms.unionpay.util.CommonUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final CPMService cpmService;
    private final MPMEmvService mpmEmvService;
    private final MPMFrontService mpmFrontService;
    private final MPMBackService mpmBackService;
    private final MPMService mpmService;

    /**
     * QR 결제요청 처리
     * MPM : URL-Front | URL-Back
     *
     * @param dto 요청 데이터
     * @return 응답 데이터
     */
    @PostMapping(value = "/request", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> paymentRequest(@Valid @RequestBody ApiMsgDto dto) {
        boolean isUrlPayload = CommonUtil.isValidUrl(dto.getTrxInfo().getMpqrcPayload());
        boolean isUrlBackStd = dto.getTrxInfo().getMpqrcPayload().startsWith("https://qr.95516.com"); // 유니온페이 표준 Back-End

        if (isUrlBackStd || !isUrlPayload) {
            return mpmService.processMpqrcPaymentUrl(dto);
        } else {
            return mpmFrontService.processMpmUrlFrontRequest(dto);
        }
    }

    /**
     * QR 결제상태 확인
     *
     * @param dto 요청 데이터
     * @return 응답 데이터
     */
    @PostMapping(value = "/check", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMsgDto paymentCheck(@Valid @RequestBody ApiMsgDto dto) {
        return mpmService.processTrxResultInquiry(dto);
    }

    /**
     * 사용자 제시 결제 QR 생성
     *
     * @param dto 요청 데이터
     * @return 응답 데이터
     */
    @PostMapping(value = "/qr/generate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMsgDto paymentQrGenerate(@Valid @RequestBody ApiMsgDto dto) {
        return cpmService.processCpqrcGeneration(dto);
    }

    /**
     * MPM QR 스캔
     * Emv 결제요청 | URL-Front QR 검증 | URL-Back QR 검증
     *
     * @param dto 요청 데이터
     * @return 응답 데이터
     */
    @PostMapping(value = "/qr/scan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMsgDto paymentQrScan(@Valid @RequestBody ApiMsgDto dto) throws Exception {
        String mpqrcPayload = dto.getTrxInfo().getMpqrcPayload();

        String[] weChatUrlList = {
                // Personal Code
                "https://wx.tenpay.com/f2f",
                "wxp://f2f",
                "https://payapp.weixin.qq.com/qr/",
                "https://payapp.wechatpay.cn/qr/",

                // Business Code
                "https://payapp.weixin.qq.com/materialqr/entry/",
                "https://payapp.wechatpay.cn/materialqr/entry/",
                "https://payapp.weixin.qq.com/qrpay/order/",
                "https://payapp.wechatpay.cn/qrpay/order/",
                "https://wxpay.tenpay.com/md/",
                "https://wxpay.wechatpay.cn/md/",

                // SME Code
                "https://payapp.weixin.qq.com/sjt/qr/",
                "https://payapp.wechatpay.cn/sjt/qr/",
                "https://sjt.wxpapp.wechatpay.cn/lnk/qr/"
        };

        boolean isEmvPayload = mpqrcPayload.startsWith("0002");
        boolean isUrlBackEndPayload = mpqrcPayload.startsWith("https://qr.95516.com");
        boolean isWeChatUrl = false;

        for (String prefix : weChatUrlList) {
            if (mpqrcPayload.startsWith(prefix)) {
                isWeChatUrl = true;
                break;
            }
        }

        if(isEmvPayload) {
            Map<String, String> tlvMap = CommonUtil.parseTLV(mpqrcPayload);
            if(tlvMap.get("15").isEmpty()) {
                // 52 필드가 6011인 경우 잘못된 요청 에러 처리
                if("6011".equals(tlvMap.get("52"))) {
                    throw new BadRequestException("잘못된 요청입니다. (Invalid QR Code)");
                }
                
                return mpmEmvService.processMpqrcPaymentEmv(dto);
            }
            return mpmBackService.processQrcInfoInquiry(dto);
        } else if(isUrlBackEndPayload) {
            return mpmBackService.processQrcInfoInquiry(dto);
        } else if(isWeChatUrl) {
            String weChatFrontUrl = "https://qr.95516.com/qrcGtwWeb-web/api/open?qrCode=";
            weChatFrontUrl += CommonUtil.encodeUrl(mpqrcPayload);
            dto = dto.toBuilder()
                    .trxInfo(dto.getTrxInfo().toBuilder()
                            .mpqrcPayload(weChatFrontUrl)
                            .build())
                    .build();
        }

        return mpmFrontService.processUserAuth(dto);
    }
}