import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TlvTest {

    static class TlvElement {
        String tag;
        int length;
        String value;

        public TlvElement(String tag, int length, String value) {
            this.tag = tag;
            this.length = length;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("Tag: %s | Length: %d | Value: %s", tag, length, value);
        }
    }

    @Test
    public void testBase64TlvDecode() {
        // 예제: 4F07A0000000031010 5A0812345678901234 → TLV 구조로 base64 인코딩
        String base64Tlv = "hQVDUFYwMWFWTwigAAADMwEBAlcRYpJpBjMXaHLTAQIBAAABiAFfNAEAYzOfJghWiw0DPsFfEJ8nAYCfEBEHAAEDoAAAAQgzOTk5MDMxM582Av4+ggIAAJ83BADpyoQ=";

        byte[] tlvBytes = Base64.getDecoder().decode(base64Tlv);
        List<TlvElement> parsed = parseTlv(tlvBytes);

        System.out.println("=== TLV 디코딩 결과 ===");
        for (TlvElement el : parsed) {
            System.out.println(el);
        }
    }

    private List<TlvElement> parseTlv(byte[] data) {
        List<TlvElement> elements = new ArrayList<>();
        int index = 0;

        while (index < data.length) {
            // Tag
            StringBuilder tagBuilder = new StringBuilder();
            tagBuilder.append(String.format("%02X", data[index++]));
            if ((Integer.parseInt(tagBuilder.toString(), 16) & 0x1F) == 0x1F) {
                tagBuilder.append(String.format("%02X", data[index++]));
            }
            String tag = tagBuilder.toString();

            // Length
            int length = data[index++] & 0xFF;
            if ((length & 0x80) != 0) { // long-form length
                int numLengthBytes = length & 0x7F;
                length = 0;
                for (int i = 0; i < numLengthBytes; i++) {
                    length = (length << 8) | (data[index++] & 0xFF);
                }
            }

            // Value
            byte[] valueBytes = new byte[length];
            System.arraycopy(data, index, valueBytes, 0, length);
            String value = bytesToHex(valueBytes);
            index += length;

            elements.add(new TlvElement(tag, length, value));
        }

        return elements;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
