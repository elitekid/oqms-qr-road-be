import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CpmTest {

    @Test
    public void createCPMPayloadTest() {
        // CPM Payload 구성
        Map<String, String> emvTags = new LinkedHashMap<>();
        emvTags.put("85", "CPV01");  // Payload Format Indicator
        emvTags.put("61", "4F08F32865QB010101");  // Application Template (AID 포함)
        emvTags.put("9F10", "07000103A0000001083339393930333133"); // 계좌이체 기반 결제 서비스 (하나은행)
        emvTags.put("9F26", "568B0D033EC15F10"); // Dummy 데이터
        emvTags.put("9F27", "80"); // Dummy 데이터
        emvTags.put("9F36", "FE3E"); // Dummy 데이터
        emvTags.put("82", "0000"); // Dummy 데이터
        emvTags.put("9F37", "00E9CA84"); // Dummy 데이터

        // CPM Payload 문자열로 변환
        StringBuilder cpmPayload = new StringBuilder();
        for (Map.Entry<String, String> entry : emvTags.entrySet()) {
            cpmPayload.append(entry.getKey()).append(entry.getValue());
        }

        // Base64 인코딩
        log.info("payload: {}", Base64.getEncoder().encodeToString(cpmPayload.toString().getBytes(StandardCharsets.UTF_8)));
    }
}
