import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Slf4j
public class CertificationTest {

    @Test
    void keyEncodeTestByFiles() {
        // 인증서 파일 경로 (클래스패스 기준)
        String signatureCertFilePath = "certs/app_public_signature_2048.cer";
        String encryptionCertFilePath = "certs/app_public_encrypt_2048.cer"; // 암호화용 인증서 경로

        // 인증서 파일 경로 출력
        System.out.println("Signature Public Key Certificate Path: " + signatureCertFilePath);
        System.out.println("Encryption Public Key Certificate Path: " + encryptionCertFilePath);

        // ClassPathResource를 사용하여 인증서 파일 읽기
        ClassPathResource signatureCertResource = new ClassPathResource(signatureCertFilePath);
        ClassPathResource encryptionCertResource = new ClassPathResource(encryptionCertFilePath);

        try (InputStream signatureCertInputStream = signatureCertResource.getInputStream();
             InputStream encryptionCertInputStream = encryptionCertResource.getInputStream()) {

            // 인증서 파일을 X.509 인증서 객체로 읽기
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            // 서명 인증서에서 공개 키 추출
            X509Certificate signatureCert = (X509Certificate) certificateFactory.generateCertificate(signatureCertInputStream);
            PublicKey signaturePublicKey = signatureCert.getPublicKey();
            String signaturePublicKeyString = Base64.getEncoder().encodeToString(signaturePublicKey.getEncoded());
            String signatureCertId = signatureCert.getSerialNumber().toString();

            // 암호화 인증서에서 공개 키 추출
            X509Certificate encryptionCert = (X509Certificate) certificateFactory.generateCertificate(encryptionCertInputStream);
            PublicKey encryptionPublicKey = encryptionCert.getPublicKey();
            String encryptionPublicKeyString = Base64.getEncoder().encodeToString(encryptionPublicKey.getEncoded());
            String encryptionCertId = encryptionCert.getSerialNumber().toString();

            // Base64로 인코딩된 공개 키 출력
            System.out.println("-----------------Signature Public--------------------");
            System.out.println("Signature Public Key: " + signaturePublicKeyString);
            System.out.println("Signature Cert ID: " + signatureCertId);
            System.out.println("-----------------Signature Public--------------------");

            System.out.println("-----------------Encryption Public--------------------");
            System.out.println("Encryption Public Key: " + encryptionPublicKeyString);
            System.out.println("Encryption Cert ID: " + encryptionCertId);
            System.out.println("-----------------Encryption Public--------------------");

        } catch (IOException | java.security.cert.CertificateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void keyEncodeTestByString() {
        // 인증서 내용 (CER 파일 내용을 문자열로 전달받은 것이라고 가정)
        String signatureCertContent = "MIIDWzCCAkOgAwIBAgIEeLOELTANBgkqhkiG9w0BAQsFADBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMB4XDTI1MDMwNzA4NDAzM1oXDTI1MDQwNjA4NDAzM1owRTELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK3T/lRGckKOZZCsxd6/Mr2ZypGOFngO4aVVCZM6oTQjfZuoGgVM7ECeu4YtT8ctUZmfcxGByM05ZeU/C3tL/opq2GtMCmK2vYMaVA60CRPE4LTnmKnax28onH8U8bJZ7+HaATzF1yKfQLid0GfVv74KXdUAIWxnVIh5nwp+O/Rd5V3Y5vVZFDTYGPTNaCZ0nx/O1gsQhAHFsbEM/007QJ27OpxfgsNQx8RWj8Ig3OObo7GS2R0lNkVB/UjBwN070SmB539BSCZNyvlxiL51kpEcIGujKytZQ39yV1ogfqVP8dQ+B7a1MA5BO5USlFIbC6kFfaFgfo5GZjHTmWYDnocCAwEAAaNTMFEwHQYDVR0OBBYEFAXWQpUmeeXfziMB3U0QItH4LpylMB8GA1UdIwQYMBaAFAXWQpUmeeXfziMB3U0QItH4LpylMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBACkwa+zS8Rm46TICjHkf+VGAeHtm+GWP47QyF9NoO5GbIzvxfn+waHo2oRWgO7CyvIGhvwjgYwuh86kXBEYWBaQ9umqM2iWNvrzeBrYFRP4omvjCll/WqETh/gB9MmlJ07nim4OzMr1s+QNvw22Q6+Z7ln8sezrOXEwOQCOTX35Un2dXGGLzOUgEqPuQsQYWOS0MiySK9b+E6bFV3vzNXfKIns6+BoHu+LHARfy/5GOyXAtpr5qjU9rr6W6KcavcRiGiCybTyUW3Fv/wLHoXsVNyBA0lSHB/rR469dp1W6y+XqDvxhrZ+Wx5KZPQI6xIGdw/1s8L3DSH+XR8YjUdyVY="; // 여기에 서명 인증서의 CER 내용 복사
        String encryptionCertContent = "MIIDWzCCAkOgAwIBAgIEeLOELjANBgkqhkiG9w0BAQsFADBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMB4XDTI1MDMwNzA4NDQzNloXDTI1MDQwNjA4NDQzNlowRTELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIlzUhzQUFS52Pov5vJZklhEDo4sh8Ex+xB3cAj9Rr09/RIg1acPMoHgVs+djUzcUHZL1OjyCVBazRXXRCzBFN0NaTVaKM2mNPbIRMvvXrUa+YqVbbBOLiFC+q8gVHVafYGaLhFmhKEE31LBSbe15Tla8E2Z78L2trBzUSg+0fYDGyWHlr9l2r7aOPOdSFvCBC8C6hpAkpxgEZapUHI32vWYCmsYBC1jqGPjvVuPqNxoMlCgKuoT9UbcjmiDPeGLaVnNQhpZ728YoodzLgafJaR2mNkHVcog5XthLtJJmidBDhm5Z9sbQQj1o8q+0c699MuzthlbX7RvStOPD/Dza00CAwEAAaNTMFEwHQYDVR0OBBYEFDjyQAUSRl1c2l/ZwUsxAba+kHO7MB8GA1UdIwQYMBaAFDjyQAUSRl1c2l/ZwUsxAba+kHO7MA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBADv7iaFUOIIAEYSBSvTw8PfeT3VTVLkSZmdDKmn2/3DOvTAGzEFR+oG+rQ050TmTiB+GhH5Sart6UUHKDO9LRbuY8OZz7NnLfgdT6fiDR9jKn+ZWDP2Fn7LnYiL3tBBWu+kc+3/cLtbxbLzGvBNz2Ej47XYTGMNhz88T/V14n2AOJkGFtaYgSVdG2mBqWukUJF3S0x8kVk8GDIdV1GsqLmGzu804cJL+ySPlUyIEdxmmgjtDb6KouOBe7mi8CXNsgRn1wDNgcAS3DkKQqnfFmU7Hzh2hPhnkjX/n/waLueASOWaJCEKWrwSlNPZmHi/r9CPvjpZI6FXKCwUaBhlv44g="; // 여기에 암호화 인증서의 CER 내용 복사
//        String signatureCertContent = "MIIDWzCCAkOgAwIBAgIEeKWlyTANBgkqhkiG9w0BAQsFADBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMB4XDTI0MTIxODA5MzYyOVoXDTI1MDExNzA5MzYyOVowRTELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAN1P6+L0HmrvBVneoN18AU32D8OrpxcumNADexmfbUfdVbhBQGSjP/FnSoITnbJpmwyRap+FWPOY5tDVrzF66CaOPlU1fWBdOg9thcurA1ZGO/UVwDRwQcSyFF8yqdZ6rrLNmJYoV9xtdGUCmlLEZMeXZIuiAqia3dBAxff94HvY4KqPcInIA3ccDFeNX+9b3uuyWnYQlYruV1bJ9ezUszljqFZNmkaKP3UL/7g1mPvQGL+tFl4dnmrhog0XRtIUFyQObnF2cHS7JYz3br/oyL/Acrxfos2UhP9nosd1nFLPvGiRUxPv+7PSlYtO4312AAlwrm5AU6lZz+3zen6mOGkCAwEAAaNTMFEwHQYDVR0OBBYEFO108NsDR+Fkl8Nou2iA567JefF8MB8GA1UdIwQYMBaAFO108NsDR+Fkl8Nou2iA567JefF8MA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAF8sA2SjS3UzJxChotDSbqEoY7861avcUtCGODKtQu+Q5uV1piRJoGO6hG0SGPFnGaakGERrHtUTNwJRAr+ypkv+UYsk3UVXQysduGXjUNW65XLy767L+DFAKeYoKmUHm/vdAc/Ex6dqb4mPjV7aNkbMtySTEzYI/TUBhpFUm8w0c8Y+RDR1P1KEMF9lfk7hNk1mH5i1RaZfPW2pf925O8b+Ipg7H+0c0ljJjJ9vmQlZBNWkvEHS9aa6PXxiSNoRXawwole88WPfOpiLUpQgtQjLunlFYKfKbiIsDxTyVpg6h/AJHoSRZY8+e1YKjBr/Lx28nrpozM/EjANuy4Mbjus="; // 여기에 서명 인증서의 CER 내용 복사
//        String encryptionCertContent = "MIIDWzCCAkOgAwIBAgIEeKWlyjANBgkqhkiG9w0BAQsFADBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMB4XDTI0MTIxODA5MzY1M1oXDTI1MDExNzA5MzY1M1owRTELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKwf/xZmI0txEGtty3W9n91MZZlLO9ssQ17J3q95VZgYWcqXnxt4SZKkIe8kP/8MdvkMJEQcCYnI48A16FyI+Glm7zsR2ZDrBSbhNSFKVEKdT3QK3VVbti9wmHkdKMcc5JGXgV0X4T7dq7KEPxQTz2sitvMef7ZXGQiGa6UA3eOvrsDP8hv0YwvsnsSAr057fx6aATvlFb0K6BRDrUIvljwmsF2QiBKMHSb+bFMSgkKdU5dwAWYcrvLUNTlkYoTK2PXkXeT9RnQsqtBxLqFVuQYl4nle1aj319DubcL/3xwvASKbg6XLG0ByhffaW+/x2YirbTMkJ8ndCnLUSFFyGSsCAwEAAaNTMFEwHQYDVR0OBBYEFMfuRq1Y3qMhb9FFFYBQ4O3vrWBzMB8GA1UdIwQYMBaAFMfuRq1Y3qMhb9FFFYBQ4O3vrWBzMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAG/2qR9TySI0skFdEwADQ0u4n4tLDzScLkOL+tod1hACk+tS//Cisk6I2/e0L6njN2R+Rdw6nTHXZAUaecUxvOuzn41scMm4PDT7OSl0iiEk8jobEQQzC1h1Q0ncYZ/WJgb+EpmHnpVng/thboj2cE/bXi5pgF/KtYdU+BlniaqKbth/OzmoGX0FtMGQBF1OD/NFaIAlAT6T0Mdy59AWxNb+7jXCF0WFP/fIzre0ebHN7GS3yrfGU7ya9TT2qHLmmPqQ038cWeQdXAwqnJwxZy6kVmSkyogSBug5BIw7pGKwJAg0i488MXRfHXEDJFTDYCt3slc7m0qtFnyQ5ssgoB0="; // 여기에 암호화 인증서의 CER 내용 복사

        try {
            // 인증서 내용으로부터 X.509 인증서 객체 생성
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            // 서명 인증서에서 공개 키 추출
            byte[] signatureCertBytes = Base64.getDecoder().decode(signatureCertContent);
            InputStream signatureCertInputStream = new ByteArrayInputStream(signatureCertBytes);
            X509Certificate signatureCert = (X509Certificate) certificateFactory.generateCertificate(signatureCertInputStream);
            PublicKey signaturePublicKey = signatureCert.getPublicKey();
            String signaturePublicKeyString = Base64.getEncoder().encodeToString(signaturePublicKey.getEncoded());
            String signatureCertId = signatureCert.getSerialNumber().toString();

            // 암호화 인증서에서 공개 키 추출
            byte[] encryptionCertBytes = Base64.getDecoder().decode(encryptionCertContent);
            InputStream encryptionCertInputStream = new ByteArrayInputStream(encryptionCertBytes);
            X509Certificate encryptionCert = (X509Certificate) certificateFactory.generateCertificate(encryptionCertInputStream);
            PublicKey encryptionPublicKey = encryptionCert.getPublicKey();
            String encryptionPublicKeyString = Base64.getEncoder().encodeToString(encryptionPublicKey.getEncoded());
            String encryptionCertId = encryptionCert.getSerialNumber().toString();

            // Base64로 인코딩된 공개 키 출력
            System.out.println("-----------------Signature Public--------------------");
            System.out.println("Signature Public Key: " + signaturePublicKeyString);
            System.out.println("Signature Cert ID: " + signatureCertId);
            System.out.println("-----------------Signature Public--------------------");

            System.out.println("-----------------Encryption Public--------------------");
            System.out.println("Encryption Public Key: " + encryptionPublicKeyString);
            System.out.println("Encryption Cert ID: " + encryptionCertId);
            System.out.println("-----------------Encryption Public--------------------");

        } catch (java.security.cert.CertificateException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadAndPrintCertificate() {
        String signatureCertText = "-----BEGIN CERTIFICATE-----\n...base64...\n-----END CERTIFICATE-----";
        String encryptionCertText = "-----BEGIN CERTIFICATE-----\n...base64...\n-----END CERTIFICATE-----";

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            // 텍스트로부터 인증서 읽기 (PEM 문자열에서 헤더 제거)
            X509Certificate signatureCert = parseCertificateFromString(signatureCertText, certificateFactory);
            X509Certificate encryptionCert = parseCertificateFromString(encryptionCertText, certificateFactory);

            // 공개키 추출 및 Base64 인코딩
            PublicKey signaturePublicKey = signatureCert.getPublicKey();
            PublicKey encryptionPublicKey = encryptionCert.getPublicKey();

            String signaturePublicKeyString = Base64.getEncoder().encodeToString(signaturePublicKey.getEncoded());
            String encryptionPublicKeyString = Base64.getEncoder().encodeToString(encryptionPublicKey.getEncoded());

            String signatureCertId = signatureCert.getSerialNumber().toString();
            String encryptionCertId = encryptionCert.getSerialNumber().toString();

            System.out.println("-----------------Signature Public--------------------");
            System.out.println("Signature Public Key: " + signaturePublicKeyString);
            System.out.println("Signature Cert ID: " + signatureCertId);
            System.out.println("-----------------Signature Public--------------------");

            System.out.println("-----------------Encryption Public--------------------");
            System.out.println("Encryption Public Key: " + encryptionPublicKeyString);
            System.out.println("Encryption Cert ID: " + encryptionCertId);
            System.out.println("-----------------Encryption Public--------------------");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static X509Certificate parseCertificateFromString(String certText, CertificateFactory factory) throws Exception {
        // PEM 포맷 헤더/푸터 제거
        String base64Cert = certText
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");

        byte[] certBytes = Base64.getDecoder().decode(base64Cert);
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }
}
