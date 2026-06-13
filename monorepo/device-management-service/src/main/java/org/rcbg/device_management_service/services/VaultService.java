package org.rcbg.device_management_service.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;

import java.util.Collections;
import java.util.List;
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
                .put(path, data);
        log.info("Saved secret {} successfully", path);
    }

    public void deleteKeyValueSecret(String path) {
        log.info("Deleting secret in path {}", path);
        vaultTemplate
                .opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.versioned())
                .delete(path);
        log.info("Deleted secret {} successfully", path);
    }

    public List<String> listSecretKeys(String basePath) {
        log.info("Listing secrets for path: {}", basePath);
        List<String> keys = vaultTemplate
                .opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.versioned())
                .list(basePath);
        return keys != null ? keys : Collections.emptyList();
    }
}
