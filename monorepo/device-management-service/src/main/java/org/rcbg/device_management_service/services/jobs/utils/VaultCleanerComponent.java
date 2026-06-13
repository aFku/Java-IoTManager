package org.rcbg.device_management_service.services.jobs.utils;

import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.repositories.DeviceRepository;
import org.rcbg.device_management_service.services.VaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class VaultCleanerComponent {

    @Value("${spring.cloud.vault.kv.default-context}")
    private String basePath;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private VaultService vaultService;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final int batchSize = 512;

    public void run() throws InterruptedException {
        log.info("Fetching list of entries from {}", basePath);
        List<String> vaultSecrets = vaultService.listSecretKeys(basePath);
        log.info("Found {} entries in vault", vaultSecrets.size());

        AtomicInteger deletedCount = new AtomicInteger(0);
        for (int i = 0; i < vaultSecrets.size(); i += batchSize) {
            int batchStart = i;
            executor.submit(() -> {
                List<UUID> batch = vaultSecrets.subList(batchStart, Math.min(vaultSecrets.size(), batchStart + batchSize))
                        .stream()
                        .filter(this::isValidUUID)
                        .map(UUID::fromString)
                        .toList();;
                Set<UUID> existingDevices = deviceRepository.findExistingDeviceIds(batch);
                Set<UUID> batchSet = new HashSet<>(batch);
                batchSet.removeAll(existingDevices);
                batchSet.forEach(element -> {
                    String fullPath = basePath + "/" + element.toString();
                    vaultService.deleteKeyValueSecret(fullPath);
                    deletedCount.incrementAndGet();
                });
                long threadId = Thread.currentThread().threadId();
                log.info("[ThreadID: {}] Removed {} entries from vault", threadId, batchSet.size());
            });
        }
        executor.shutdown();

        executor.awaitTermination(60, TimeUnit.MINUTES);
        log.info("VaultCleaner removed {} orphaned secrets", deletedCount.get());
    }

    private boolean isValidUUID(String str) {
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("Incorrect UUID: {}", str);
            return false;
        }
    }
}
