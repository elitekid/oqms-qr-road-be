package com.qrroad.oqms.unionpay.keymanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrroad.oqms.unionpay.dto.certificate.AppCertificateDto;
import com.qrroad.oqms.unionpay.dto.certificate.PrivateCertificateDto;
import com.qrroad.oqms.unionpay.dto.certificate.UmpsCertificateDto;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;

import java.util.Objects;

@Component
@Getter
public class UmpsCertificateKeyManager {
    private final UmpsCertificateDto umpsKeys;
    private final AppCertificateDto appKeys;
    private final PrivateCertificateDto privateKeys;

    public UmpsCertificateKeyManager(VaultTemplate vaultTemplate) {
        ObjectMapper objectMapper = new ObjectMapper();
        VaultKeyValueOperations keyValueOperations = vaultTemplate.opsForKeyValue("/umps_certificate", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2);

        this.umpsKeys = objectMapper.convertValue(Objects.requireNonNull(keyValueOperations.get("umps")).getData(), UmpsCertificateDto.class);
        this.appKeys = objectMapper.convertValue(Objects.requireNonNull(keyValueOperations.get("app")).getData(), AppCertificateDto.class);
        this.privateKeys = objectMapper.convertValue(Objects.requireNonNull(keyValueOperations.get("private")).getData(), PrivateCertificateDto.class);
    }
}
