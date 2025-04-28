import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class UtilForDevelopment {
    @Test
    void 인증서_파일에서_내용_추출() {
        String contents = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDWzCCAkOgAwIBAgIEeKWlyjANBgkqhkiG9w0BAQsFADBFMQswCQYDVQQGEwJB\n" +
                "VTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0\n" +
                "cyBQdHkgTHRkMB4XDTI0MTIxODA5MzY1M1oXDTI1MDExNzA5MzY1M1owRTELMAkG\n" +
                "A1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0\n" +
                "IFdpZGdpdHMgUHR5IEx0ZDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB\n" +
                "AKwf/xZmI0txEGtty3W9n91MZZlLO9ssQ17J3q95VZgYWcqXnxt4SZKkIe8kP/8M\n" +
                "dvkMJEQcCYnI48A16FyI+Glm7zsR2ZDrBSbhNSFKVEKdT3QK3VVbti9wmHkdKMcc\n" +
                "5JGXgV0X4T7dq7KEPxQTz2sitvMef7ZXGQiGa6UA3eOvrsDP8hv0YwvsnsSAr057\n" +
                "fx6aATvlFb0K6BRDrUIvljwmsF2QiBKMHSb+bFMSgkKdU5dwAWYcrvLUNTlkYoTK\n" +
                "2PXkXeT9RnQsqtBxLqFVuQYl4nle1aj319DubcL/3xwvASKbg6XLG0ByhffaW+/x\n" +
                "2YirbTMkJ8ndCnLUSFFyGSsCAwEAAaNTMFEwHQYDVR0OBBYEFMfuRq1Y3qMhb9FF\n" +
                "FYBQ4O3vrWBzMB8GA1UdIwQYMBaAFMfuRq1Y3qMhb9FFFYBQ4O3vrWBzMA8GA1Ud\n" +
                "EwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAG/2qR9TySI0skFdEwADQ0u4\n" +
                "n4tLDzScLkOL+tod1hACk+tS//Cisk6I2/e0L6njN2R+Rdw6nTHXZAUaecUxvOuz\n" +
                "n41scMm4PDT7OSl0iiEk8jobEQQzC1h1Q0ncYZ/WJgb+EpmHnpVng/thboj2cE/b\n" +
                "Xi5pgF/KtYdU+BlniaqKbth/OzmoGX0FtMGQBF1OD/NFaIAlAT6T0Mdy59AWxNb+\n" +
                "7jXCF0WFP/fIzre0ebHN7GS3yrfGU7ya9TT2qHLmmPqQ038cWeQdXAwqnJwxZy6k\n" +
                "VmSkyogSBug5BIw7pGKwJAg0i488MXRfHXEDJFTDYCt3slc7m0qtFnyQ5ssgoB0=\n" +
                "-----END CERTIFICATE-----\n";


        String startTag = "-----BEGIN CERTIFICATE-----";
        String endTag = "-----END CERTIFICATE-----";

        int start = contents.indexOf(startTag) + startTag.length();
        int end = contents.indexOf(endTag);
        System.out.println("Signature Public Key: " + contents.substring(start, end).replaceAll("[\\r\\n]+", "").trim());
    }
}
