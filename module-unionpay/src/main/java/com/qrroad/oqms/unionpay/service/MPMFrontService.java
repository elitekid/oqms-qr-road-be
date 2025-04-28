package com.qrroad.oqms.unionpay.service;

import com.qrroad.oqms.domain.dto.OqUpiUserAuthDto;
import com.qrroad.oqms.domain.repository.OqAdminUsersRepository;
import com.qrroad.oqms.domain.repository.OqUpiUserAuthRepository;
import com.qrroad.oqms.unionpay.config.HttpGetWithEntity;
import com.qrroad.oqms.unionpay.dto.*;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.GetUserIdTrxInfo;
import com.qrroad.oqms.unionpay.dto.upitrxinfo.OrderVerifyTrxInfo;
import com.qrroad.oqms.unionpay.enums.ApiResponseCode;
import com.qrroad.oqms.unionpay.enums.ApiSource;
import com.qrroad.oqms.unionpay.enums.MessageId;
import com.qrroad.oqms.unionpay.enums.MessageType;
import com.qrroad.oqms.unionpay.keymanagement.UmpsCertificateKeyManager;
import com.qrroad.oqms.unionpay.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MPMFrontService {
    private static final int CODE_LENGTH = 20;

    private final UmpsCertificateKeyManager umpsCertificateKeyManager;
    private final OqUpiUserAuthRepository userAuthRepository;
    private final TokenService tokenService;
    private final QrPayService qrPayService;
    private final MPMService mpmService;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 가맹점제시 QR 결제시 사전 판매자 정보 확인
     * - Step1. 매입사 url 호출 및 사용자 임시 인증코드 전달 url 생성.
     * - Step2. 사용자 인증 url 요청 및 사용자 주문 확인 페이지 응답 받아 필요 정보를 사용자에 전달.(중간에 GET_USER_ID 요청받아 수행)
     * ** 1, 2 과정의 DB Insert / Update 는 트랜잭션 분리가 필요하기 때문에 메소드 분리.
     *
     * @param  dto 단말기로부터 요청 데이터
     * @return 은행으로 응답 데이터
     */
    public ApiMsgDto processUserAuth(ApiMsgDto dto) {
        String userAuthCode = generateRandomCode();

        // Step1
        String callBackUrl = processTransaction1(dto, userAuthCode);

        // Step2
        return processTransaction2(dto, userAuthCode, callBackUrl);
    }

    /**
     * 매입사 url 호출 및 사용자 임시 인증코드 전달 url 생성
     * 1. 매입사에 요청 후 응답받은 302 redirect Url 추출
     * 2. 사용자 인증 요청 url 생성
     *
     * @param  dto 단말기로부터 요청 데이터
     * @return 은행으로 응답 데이터
     */
    @Transactional
    private String processTransaction1(ApiMsgDto dto, String userAuthCode) {
        dto = dto.toBuilder()
                .trxInfo(dto.getTrxInfo().toBuilder().txnID(CommonUtil.generateTnxID())
                        .build())
                .build();

        String originalUrl = getRedirectUrl(dto.getTrxInfo().getMpqrcPayload());
        log.info("302 redirect URL : {}", originalUrl);

        // 1. URL 에서 'redirectUrl' 파라미터 추출
        String redirectUrl = extractRedirectUrl(originalUrl);

        // 2. 중첩된 URL 인코딩을 모두 디코딩
        String decodedRedirectUrl = URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8);
        String finalUrl = URLDecoder.decode(decodedRedirectUrl, StandardCharsets.UTF_8);

        finalUrl += "&respCode=00&userAuthCode=" + userAuthCode;
        log.info("redirectUrl : {}", finalUrl);

        insertAuthCode(dto, userAuthCode);

        return finalUrl;
    }

    /**
     * 사용자 인증 url 요청 및 사용자 주문 확인 페이지 응답 받아 필요 정보를 사용자에 전달.(중간에 GET_USER_ID 요청받아 수행)
     * 1. 사용자 인증 url 요청. 응답 전 GET_USER_ID 로직이 수행됨.
     * 2. 결제정보 추출 및 반환
     *
     * @param  dto 단말기로부터 요청 데이터
     * @return 은행으로 응답 데이터
     */
    @Transactional
    private ApiMsgDto processTransaction2(ApiMsgDto dto, String userAuthCode, String callBackUrl) {
        // 1. 사용자 인증 url 요청. 응답 전 GET_USER_ID 로직이 수행됨.
        ResponseEntity<?> entity = callBackUrl(callBackUrl);
        log.info("entity : {}", entity);

        // 2. 결제정보 추출 및 반환
        String responseBody = (String) entity.getBody();
        Map<String, String> formData = extractFormData(responseBody);
        log.info("formData : {}", formData);

        if(formData.isEmpty()) {
//            throw new CustomException(OTHER_ERROR.getCode(), OTHER_ERROR.getMessage());
        }

        updateAuthCode(dto, formData, userAuthCode);

        dto = dto.toBuilder()
                .trxInfo(dto.getTrxInfo().toBuilder()
                        .appUserID(formData.get("userId"))
                        .trxAmt(formData.get("txnAmt"))
                        .build())
                .msgResponse(dto.getMsgResponse().toBuilder()
                        .responseMsg("Approved")
                        .responseCode("00")
                        .build())
                .build();
        return dto;
    }

    /**
     * Mpm Front-end 결제요청
     * - 1. 사용자 주문정보 확인
     * - 2. ORDER_VERIFY 요청 / 응답
     * - 3. MPQRC_PAYMENT_URL 요청 / 응답
     *
     * @param  apiDto 단말기로부터 요청 데이터
     * @return 은행으로 응답 데이터
     */
    @Transactional
    public ResponseEntity<?> processMpmUrlFrontRequest(ApiMsgDto apiDto) {

        // 주문정보 Confirm 하기 위한 파리미터 생성
        String encodedParams = encodeConfirmParams(apiDto);

        // 1. 주문정보 Confirm 요청 / 응답
        String responseBody = confirmOrder(encodedParams);

        // 응답에서 ORDER_VERIFY 요청을 위한 payOrderInfo 추출
        String payOrderInfo = getPayOrderInfo(responseBody);
        log.info(payOrderInfo);

        // 2. ORDER_VERIFY 요청 / 응답
        UpiMsgDto<OrderVerifyTrxInfo> requestUpiDto = generateOrderVerifyRequestUpiDto(apiDto, payOrderInfo);
        UpiMsgDto<OrderVerifyTrxInfo> responseUpiDto = processOrderVerify(requestUpiDto, apiDto.getPayInfo());

        if(!responseUpiDto.getMsgResponse().getResponseCode().equals("00")) {
//            ErrorResponseToBank response = new ErrorResponseToBank();
//            response.setMsgResponse(responseUpiDto.getMsgResponse());
//            return ResponseEntity.ok(response);
        }

        // 3. MPQRC_PAYMENT_URL 결제요청
        ApiMsgDto mpqrcPaymentUrlRequestDto = getMpqrcPaymentUrlRequestBankDto(apiDto, responseUpiDto);
        return  mpmService.processMpqrcPaymentUrl(mpqrcPaymentUrlRequestDto);
    }

    /**
     * 가맹점제시 QR 결제시 사전 주문 정보 확인
     *
     * @param upiDto 요청 데이터
     * @return 응답 데이터
     */
    public UpiMsgDto<OrderVerifyTrxInfo> processOrderVerify(UpiMsgDto<OrderVerifyTrxInfo> upiDto, PayInfo payInfo) {
//        qrPayService.createQrPayData(requestDto, payInfo);

        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiDto);

        upiDto = upiDto.toBuilder()
                .certificateSignature(
                        upiDto.getCertificateSignature()
                                .toBuilder()
                                .signature(CommonUtil.signUmps(
                                        Objects.requireNonNull(jsonBody).getBytes(),
                                        umpsCertificateKeyManager.getPrivateKeys().getAppPrivateSignature2048()
                                ))
                                .build()
                )
                .build();
        String finalJsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(upiDto);
        log.info("ORDER_VERIFY Request : {}", upiDto);

        UpiMsgDto<OrderVerifyTrxInfo> responseDto = orderVerify(finalJsonBody);
        log.info("ORDER_VERIFY Response : {}", responseDto);

        // 4. 서명 검증
        CommonUtil.verifyUmps(responseDto, umpsCertificateKeyManager.getUmpsKeys().getUmpsSignPublicKey());

//        qrPayService.updateQrPayData(responseDto);

        return responseDto;
    }

    /**
     * GET_USER_ID 요청에 대한 응답 처리
     *
     * @param requestUpiDto 요청 upiDto
     * @return String
     */
    public String getUserId(UpiMsgDto<GetUserIdTrxInfo> requestUpiDto) {
        // 0. 서명 검증
        CommonUtil.verifyUmps(requestUpiDto, String.valueOf(umpsCertificateKeyManager.getAppKeys().getAppSignPublicKey()));

        // 1. 응답 DTO 생성
        UpiMsgDto<GetUserIdTrxInfo> responseUpiDto = generateGetUserIdResponseDto(requestUpiDto);

        // 2. To-Be-Signed String 변환 (서명할 값)
        String jsonBody = CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(responseUpiDto);

        // 3. 서명 / 응답 보낼 값으로 변환
        responseUpiDto = responseUpiDto.toBuilder()
                .certificateSignature(
                        responseUpiDto.getCertificateSignature()
                                .toBuilder()
                                .signature(CommonUtil.signUmps(
                                        Objects.requireNonNull(jsonBody).getBytes(),
                                        umpsCertificateKeyManager.getPrivateKeys().getAppPrivateSignature2048()
                                ))
                                .build()
                )
                .build();
        String finalJsonBody =  CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(responseUpiDto);
        log.info("GET_USER_ID response : {}", responseUpiDto);

        return finalJsonBody;
    }

    /**
     * 302 최초 URL 에서 실제 요청할 URL 추출
     *
     * @param  url original URL
     * @return 은행으로 응답 데이터
     */
    private String extractRedirectUrl(String url) {
        try {
            String[] urlParts = url.split("\\?");
            if (urlParts.length <= 1) {
//                throw new CustomException("99", "No query parameters found in the URL");
            }

            String[] queryParams = urlParts[1].split("&");
            for (String param : queryParams) {
                String[] paramParts = param.split("=");
                if (paramParts[0].equals("redirectUrl")) {
                    if (paramParts.length < 2) {
//                        throw new CustomException("99", "redirectUrl parameter has no value");
                    }
                    return paramParts[1];
                }
            }
//            throw new CustomException("99", "redirectUrl parameter not found in the URL");
        } catch (Exception e) {
            throw e;
        }
        return url;
    }

    public Map<String, String> extractFormData(String responseBody) {
        Map<String, String> formData = new HashMap<>();
        Document doc = Jsoup.parse(responseBody);

        // 모든 form 태그 추출
        Elements forms = doc.select("form");

        for (Element form : forms) {
            // form 안의 input 태그들 추출
            Elements inputs = form.select("input");
            for (Element input : inputs) {
                String name = input.attr("name");
                String value = input.attr("value");

                if (!name.isEmpty()) {
                    formData.put(name, value);
                }
            }
        }

        if (formData.containsKey("params")) {
            String params = formData.get("params");
            String decodedParams = URLDecoder.decode(params, StandardCharsets.UTF_8);

            for (String param : decodedParams.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    formData.put(pair[0], pair[1]);
                } else {
                    formData.put(pair[0], "");
                }
            }
        }

        return formData;
    }

    private ApiMsgDto getMpqrcPaymentUrlRequestBankDto(ApiMsgDto apiDto, UpiMsgDto<OrderVerifyTrxInfo> orderVerifyDto) {
        PayInfo payInfo = PayInfo.builder()
                .trxCl(apiDto.getPayInfo().getTrxCl())
                .instCd(apiDto.getPayInfo().getInstCd())
                .build();

        ApiMsgDto.TrxInfo trxInfo = ApiMsgDto.TrxInfo.builder()
                .token(apiDto.getTrxInfo().getToken())
                .txnID(orderVerifyDto.getTrxInfo().getTxnID())
                .mpqrcPayload(apiDto.getTrxInfo().getMpqrcPayload())
                .trxAmt(orderVerifyDto.getTrxInfo().getTrxAmt())
                .build();

        return ApiMsgDto.builder()
                .payInfo(payInfo)
                .trxInfo(trxInfo)
                .build();
    }

    public UpiMsgDto<OrderVerifyTrxInfo> orderVerify(String finalJsonBody) {
        return RestClient.create()
                .post()
                .uri(ApiSource.UNION_PAY.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(finalJsonBody)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * 주문 확인 내용 인코딩
     *
     * @param apiDto 요청 데이터
     * @return encoded params
     */
    private String encodeConfirmParams(ApiMsgDto apiDto) {
        Map<String, String> params = new HashMap<>();

        params.put("action", "fillOrder");
        params.put("params", userAuthRepository.selectNextParam(apiDto.getTrxInfo().getTxnID()));

        StringBuilder encodedParams = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!encodedParams.isEmpty()) {
                encodedParams.append("&");
            }
            encodedParams.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            encodedParams.append("=");
            encodedParams.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        return encodedParams.toString();
    }

    /**
     * 주문 정보 확인
     * 원본 페이지에서 확인 버튼시 동작하는 부분을 코드로 구현.
     * GET 요청이지만 Body 를 필요로 하기 때문에 HttpGetWithEntity 로 구현 후 사용.
     *
     * @param params 원본 확인 요청의 formData
     * @return 응답 URL
     */
    private String confirmOrder(String params) {
        String url = "https://open.unionpay.com/ajweb/help/hymSimulation/pushOrder";

        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .build();

            HttpGetWithEntity request = new HttpGetWithEntity();
            request.setURI(new URI(url));
            request.setEntity(new StringEntity(params));

            request.setHeader(HttpHeaders.USER_AGENT, "UnionPay/1.0 39990313");
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            request.setHeader(HttpHeaders.ACCEPT, "application/json");

            CloseableHttpResponse response =  httpClient.execute(request);
            String responseEntity = EntityUtils.toString(response.getEntity());

            response.close();
            httpClient.close();

            return responseEntity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute GET request with body: " + e.getMessage(), e);
        }
    }

    /**
     * 주문 확인 응답에서 payOrderInfo 추출
     *
     * @param responseBody 응답 body
     * @return payOrderInfo
     */
    private String getPayOrderInfo(String responseBody) {
        try {
            Document document = Jsoup.parse(responseBody);

            // id="callPay" 요소의 href 값 추출
            Element element = document.getElementById("callPay");
            if (element == null) {
//                throw new CustomException("01", "Element with id 'callPay' not found");
            }

            String hrefValue = element.attr("href");
            if (hrefValue == null || hrefValue.isEmpty()) {
//                throw new CustomException("01", "href value is null or empty");
            }

            // URI 객체 생성
            URI uri = new URI(hrefValue);
//			String query = uri.getQuery();
            String query = uri.getRawQuery();
//			String query = hrefValue.substring(hrefValue.indexOf("?") + 1);
            if (query == null || query.isEmpty()) {
//                throw new CustomException("01", "Query value is null or empty");
            }

            return query;
        } catch (URISyntaxException e) {
//            throw new CustomException("01", "Invalid URI syntax");
        } catch (Exception e) {
//            throw new CustomException("01", "Failed to getPayOrderInfo");
        }
        return responseBody;
    }

    /**
     *  사전 주문 정보 검증 데이터 가공
     *
     * @param apiDto, payOrderInfo 단말기로부터의 요청 데이터
     * @return 응답 Dto
     */
    private UpiMsgDto<OrderVerifyTrxInfo> generateOrderVerifyRequestUpiDto(ApiMsgDto apiDto, String payOrderInfo) {
        MsgInfo msgInfo = CommonUtil.createMsgInfo(MessageId.A, MessageType.ORDER_VERIFY);

        OrderVerifyTrxInfo trxInfo = OrderVerifyTrxInfo.builder()
                .txnID(CommonUtil.generateTnxID())
                .payOrderInfo(payOrderInfo)
                .token(apiDto.getTrxInfo().getToken())
                .deviceID(apiDto.getTrxInfo().getDeviceID())
                .appUserID(apiDto.getTrxInfo().getAppUserID())
                .mobileNo(apiDto.getTrxInfo().getReservedMobileNo())
                .build();

//        trxInfo.setDeviceID(tokenService.getDeviceId(bankDto.getTrxInfo().getToken()));
//        trxInfo.setAppUserID(tokenService.getAppUserId(bankDto.getTrxInfo().getToken()));
//        trxInfo.setMobileNo(tokenService.getMobileNo(bankDto.getTrxInfo().getToken()));

        //certificateSignature 생성
        CertificateSignature certificateSignature = CertificateSignature.builder()
                .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
                .signature("00000000")
                .build();


        return UpiMsgDto.<OrderVerifyTrxInfo>builder()
                .msgInfo(msgInfo)
                .trxInfo(trxInfo)
                .certificateSignature(certificateSignature)
                .build();
    }

    /**
     *  GET_USER_ID 응답 DTO 생성
     *
     * @param requestDto UnionPay 로부터의 요청 데이터
     * @return 응답 Dto
     */
    private UpiMsgDto<GetUserIdTrxInfo> generateGetUserIdResponseDto(UpiMsgDto<GetUserIdTrxInfo> requestDto) {
        String inputCode = requestDto.getTrxInfo().getUserAuthCode();

        OqUpiUserAuthDto userAuthDto = userAuthRepository.selectUserAuthInfo(requestDto.getTrxInfo().getUserAuthCode());

        MsgResponse msgResponse = MsgResponse.builder().build();

        if(inputCode.equals(userAuthDto.getAuthCode())) {
            msgResponse = msgResponse.toBuilder()
                    .responseCode("00")
                    .responseCode("Approved")
                    .build();
        } else {
            msgResponse = msgResponse.toBuilder()
//                    .responseCode(ApiResponseCode.USER_AUTH_INVALID.getCode())
//                    .responseMsg(ApiResponseCode.USER_AUTH_INVALID.getMessage())
                    .build();
        }

        CertificateSignature certificate = CertificateSignature.builder()
                .appSignCertID(umpsCertificateKeyManager.getAppKeys().getAppSignCertId())
                .signature("00000000")
                .build();

        return UpiMsgDto.<GetUserIdTrxInfo>builder()
                .msgInfo(requestDto.getMsgInfo())
                .trxInfo(requestDto.getTrxInfo().toBuilder()
                        .userAuthCode(null)
                        .appUserId(userAuthDto.getAppUserId())
                        .build())
                .msgResponse(msgResponse)
                .certificateSignature(certificate)
                .build();
    }

    /**
     * 사용자 인증 코드 생성
     * TBD: 인증코드 자릿수
     *
     * @return CODE_LENGTH 에 따른 사용자 인증 코드
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    private String getRedirectUrl(String url) {
        return Objects.requireNonNull(RestClient.create()
                .get()
                .uri(url)
                .header(HttpHeaders.USER_AGENT, "UnionPay/1.0 39990313")
                .retrieve()
                .onStatus(status -> status.value() != 302, (request, response) -> {
                    throw new RuntimeException();
                })
                .toBodilessEntity()
                .getHeaders().getLocation()).toString();
    }

    /**
     * 사용자 인증 요청 / 응답 반환
     *
     * @param url 사용자 인증 요청 URL
     * @return 응답 URL
     */
    private ResponseEntity<?> callBackUrl(String url) {
        return RestClient.create()
                .get()
                .uri(url)
                .header(HttpHeaders.USER_AGENT, "UnionPay/1.0 39990313")
                .retrieve()
                .toEntity(String.class);
    }

    private void insertAuthCode(ApiMsgDto dto, String authCode) {
        OqUpiUserAuthDto userAuthDto = OqUpiUserAuthDto.builder()
                .token(dto.getTrxInfo().getToken())
                .mpqrcUrl(dto.getTrxInfo().getMpqrcPayload())
                .appUserId(dto.getTrxInfo().getAppUserID())
                .authCode(authCode)
                .build();

        userAuthRepository.save(userAuthDto);
    }

    private void updateAuthCode(ApiMsgDto dto, Map<String,String> formData, String authCode) {
        OqUpiUserAuthDto oqUpiUserAuthDto = OqUpiUserAuthDto.builder()
                .token(dto.getTrxInfo().getToken())
                .authCode(authCode)
                .appUserId(formData.get("userId"))
                .trxAmt(new BigDecimal(formData.get("txnAmt")))
                .nextParam(formData.get("params"))
                .build();

        userAuthRepository.save(oqUpiUserAuthDto);
    }
}