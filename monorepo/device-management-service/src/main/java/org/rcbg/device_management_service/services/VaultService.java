package org.rcbg.device_management_service.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;

import java.util.Map;

@Slf4j
@Service
public class VaultService {

    @Autowired
    private VaultTemplate vaultTemplate;

    public void saveKeyValueSecret(String path, String key, String value) {

        log.info("Saving secret with key {} to path {}", key, path);
        Map<String, String> data = Map.of(key, value);
        vaultTemplate
                .opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.versioned())
                .patch(path, data);
        log.info("Saved secret {} successfully", key);
    }
}
