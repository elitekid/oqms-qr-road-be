import com.qrroad.oqms.infrastructure.service.VaultService;
import com.qrroad.oqms.unionpay.UnionpayApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.vault.core.VaultOperations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = UnionpayApplication.class)
@Slf4j
public class VaultTest {
    @Autowired
    private VaultService vaultService;

    @Autowired
    private VaultOperations vaultOperations;

    @Test
    void a() {
        Map<String, Object> asc = vaultService.getSecretData("/umps_certificate/data/new_app");
        if(asc.get("appSignCertId") == null) {
            log.info("ok");
            log.info(asc.get("appSignCertId").toString());
        }
        String s = vaultService.encryptByDek("abc");
        log.info("map {}", asc);
        String b = vaultService.decryptByDek(s);
        log.info(b);
    }

    @Test
    void b() {
        Map<String, Object> clearedNewApp = new HashMap<>();
        clearedNewApp.put("appSignCertId", null);
        clearedNewApp.put("appSignPublicKey", null);
        clearedNewApp.put("appEncCertId", null);
        clearedNewApp.put("appEncPublicKey", null);

        vaultService.mergeAndUpdateKvValue("/umps_certificate/data/new_app", new HashMap<>());
    }


    @Test
    void keyEncodeTestByString() {

        Map<String, Object> privateData = vaultService.getSecretData("/umps_certificate/data/private");
        String signatureCertContent = privateData.get("appPublicSignature2048").toString();
        String encryptionCertContent =privateData.get("appPublicEncrypt2048").toString();

        log.info(signatureCertContent);
        log.info(encryptionCertContent);

        try {
            // 인증서 내용으로부터 X.509 인증서 객체 생성
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            // 텍스트로부터 인증서 읽기 (PEM 문자열에서 헤더 제거)
            X509Certificate signatureCert = parseCertificateFromString(signatureCertContent, certificateFactory);
            X509Certificate encryptionCert = parseCertificateFromString(encryptionCertContent, certificateFactory);

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

        } catch (java.security.cert.CertificateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
