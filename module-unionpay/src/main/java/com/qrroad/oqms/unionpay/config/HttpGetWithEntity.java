package com.qrroad.oqms.unionpay.config;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * 본 HttpGetWithEntity 클래스는 본문을 포함하는 GET 요청을 허용하기 위해 작성되었습니다.
 *
 * 기본적으로 HTTP GET 요청은 본문을 지원하지 않습니다. 이 클래스는 HttpEntityEnclosingRequestBase를 확장하여
 * GET 메서드를 오버라이드하고, GET 요청에 본문을 포함할 수 있도록 합니다. 이는 GET 요청에 본문이 필요한 특별한
 * 상황에서 유용할 수 있습니다.
 *
 * 유니온페이 Mpm Url Front-end 결제 과정 중 결제 정보 확인 페이지의 Confirm 과정에서
 * 유니온 페이 측이 POST 허용을 하지 않기 때문에 이 클래스 사용하여 요청 전송한다.
 */
public class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {
    public final static String METHOD_NAME = "GET";

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
