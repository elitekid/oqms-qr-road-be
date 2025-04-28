package com.qrroad.oqms.infrastructure.service;

import com.qrroad.oqms.infrastructure.keymanagement.SymmetricKeyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class VaultService {
    private final VaultOperations vaultOperations;
    private final SymmetricKeyManager symmetricKeyManager;

    public VaultService(VaultOperations vaultOperations, SymmetricKeyManager symmetricKeyManager) {
        this.vaultOperations = vaultOperations;
        this.symmetricKeyManager = symmetricKeyManager;
    }

    public Map<String, Object> getSecretData(String path) {
        return Optional.ofNullable(vaultOperations.read(path))
                .map(response -> Objects.requireNonNull(response.getData()).get("data"))
                .filter(data -> data instanceof Map)
                .map(data -> (Map<String, Object>) data)
                .orElse(Collections.emptyMap());
    }

    // DEK 를 통한 암호화
    public String encryptByDek(String plaintext) {
        try {
            SecretKey secretKey = new SecretKeySpec(symmetricKeyManager.getDek(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // 평문 암호화
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("AES ECB 256-bit 암호화 오류", e);
        }
    }

    // DEK 를 통한 복호화
    public String decryptByDek(String ciphertext) {
        try {
            byte[] cipherData = Base64.getDecoder().decode(ciphertext);
            SecretKey secretKey = new SecretKeySpec(symmetricKeyManager.getDek(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedData = cipher.doFinal(cipherData);
            return new String(decryptedData);
        } catch (Exception e) {
            throw new RuntimeException("AES 복호화 오류", e);
        }
    }

    // 1. KV 경로에 값 암호화 후 저장
    public void addEncryptedValue(String path, String key, String value, boolean preserveExisting) {
        try {
            // 값 암호화
            String encryptedValue = encryptByDek(value);

            // 기존 데이터를 가져올지 결정 (preserveExisting 값에 따라)
            Map<String, Object> newData;
            if (preserveExisting) {
                // 기존 데이터 가져오기
                Map<String, Object> existingData = Objects.requireNonNull(vaultOperations.read(path)).getData();

                // 기존 데이터와 병합
                newData = existingData != null && existingData.containsKey("data")
                        ? new HashMap<>((Map<String, Object>) existingData.get("data"))
                        : new HashMap<>();
                newData.put(key, encryptedValue);
            } else {
                // 새로운 데이터 생성 (기존 데이터 무시)
                newData = Map.of(key, encryptedValue);
            }

            // Vault에 저장할 데이터 생성
            Map<String, Object> requestData = Map.of("data", newData);

            // KV 경로에 업데이트
            vaultOperations.write(path, requestData);

            log.info("Path: {}, Key: {}, EncryptedValue: {}, PreserveExisting: {}",
                    path, key, encryptedValue, preserveExisting);
        } catch (Exception e) {
            throw new RuntimeException("값 추가 중 오류", e);
        }
    }


    // 2. KV 경로에서 값 가져와 복호화
    public String getDecryptedValue(String path, String key) {
        try {
            // KV 경로에서 암호화된 값 읽기
            Map<String, Object> data = Objects.requireNonNull(vaultOperations.read(path)).getData();

            // "data" 키에서 실제 값을 가져오기
            Map<String, Object> actualData = (Map<String, Object>) data.get("data");

            if (actualData == null || !actualData.containsKey(key)) {
                throw new RuntimeException("경로에서 키를 찾을 수 없습니다: " + key);
            }

            String encryptedValue = (String) actualData.get(key);

            // 값 복호화
            String decryptedValue = decryptByDek(encryptedValue);
            log.info("Path: {}, Key: {}, DecryptedValue: {}", path, key, decryptedValue);

            return decryptedValue;
        } catch (Exception e) {
            throw new RuntimeException("값 가져오기 및 복호화 중 오류", e);
        }
    }

    public String getDataFromPathByKey(String path, String key) {
        try {

            Map<String, Object> data = Objects.requireNonNull(vaultOperations.read(path)).getData();
            Map<String, Object> actualData = (Map<String, Object>) data.get("data");

            if (actualData == null || !actualData.containsKey(key)) {
                throw new RuntimeException("경로에서 키를 찾을 수 없습니다: " + key);
            }

            String encryptedValue = (String) actualData.get(key);
            log.info("Path: {}, Key: {}, EncryptedValue: {}", path, key, encryptedValue);

            return encryptedValue;
        } catch (Exception e) {
            throw new RuntimeException("암호화된 값 가져오기 중 오류", e);
        }
    }

    /**
     * Vault의 KV2 경로에서 기존 값을 유지하면서 일부 키만 업데이트
     *
     * @param path Vault 경로 (예: secret/data/my-path)
     * @param updates 업데이트할 key-value 쌍
     */
    public void mergeAndUpdateKvValue(String path, Map<String, Object> updates) {
        try {
            // 1. 기존 데이터 조회
            Map<String, Object> existingData = getKvData(path);

            // 2. 기존 값에 업데이트할 값 덮어쓰기 (병합)
            existingData.putAll(updates);

            // 3. Vault에 저장할 구조 구성 (KV2는 "data" 필드 아래 실제 데이터)
            Map<String, Object> request = Collections.singletonMap("data", existingData);

            // 4. Vault에 쓰기
            vaultOperations.write(path, request);

        } catch (Exception e) {
            throw new RuntimeException("Vault 값 병합 업데이트 실패", e);
        }
    }

    /**
     * Vault에서 KV2 방식의 데이터 읽기
     */
    private Map<String, Object> getKvData(String path) {
        Map<String, Object> readData = Objects.requireNonNull(vaultOperations.read(path)).getData();
        if (readData == null || !readData.containsKey("data")) {
            return new HashMap<>();
        }
        return new HashMap<>((Map<String, Object>) readData.get("data"));
    }
}
