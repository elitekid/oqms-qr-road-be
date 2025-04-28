package com.qrroad.oqms.infrastructure.keymanagement;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;

import java.util.Base64;
import java.util.Objects;

@Configuration
@Getter
public class SymmetricKeyManager {

    private final VaultTransitOperations transitOperations;
    private final byte[] dek; // 데이터 암호화 키 (DEK)

    public SymmetricKeyManager(VaultTemplate vaultTemplate) {
        this.transitOperations = vaultTemplate.opsForTransit("kek");

        VaultKeyValueOperations keyValueOperations  = vaultTemplate.opsForKeyValue("/dek", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2);
        String encryptedDek = Objects.requireNonNull(keyValueOperations.get("qr-road")).getRequiredData().get("1").toString();

        this.dek = loadDekFromVault(encryptedDek);
    }

    // DEK 복호화
    private byte[] loadDekFromVault(String encryptedDek) {
        return Base64.getDecoder().decode(transitOperations.decrypt("qr-road", encryptedDek));
    }
}
