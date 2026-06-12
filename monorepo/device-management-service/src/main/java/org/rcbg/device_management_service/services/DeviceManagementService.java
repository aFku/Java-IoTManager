package org.rcbg.device_management_service.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.mappers.DeviceMapper;
import org.rcbg.device_management_service.models.dto.devices.RequestDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceWithSecretDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseSecretDto;
import org.rcbg.device_management_service.models.entities.Device;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.repositories.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.vault.VaultException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DeviceManagementService {

    // TODO: Add logs

    @Autowired
    private DeviceRepository repository;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private HomeManagementService homeManagementService;
    @Autowired
    private VaultService vaultService;
    @Value("${spring.cloud.vault.kv.default-context}")
    private String deviceSecretPath;

    @Cacheable(value="devices", key="'single_' + #deviceId + '_' + #userId")
    public ResponseDeviceDto getDevice(UUID homeId, UUID userId, UUID deviceId) {
        return deviceMapper.toDto(
                findDevice(homeId, userId, deviceId, HomeAccessRole.VIEWER)
        );
    }

    @Cacheable(value="devices", key="'list_' + #homeId + '_' + #userId + '_pageNumber_' + #pageable.pageNumber + '_pageSize_' + #pageable.pageSize + '_sort_' + #pageable.sort.toString()")
    public Page<ResponseDeviceDto> getListOfDevices(UUID homeId, UUID userId, Pageable pageable) {
        Home home = findHome(homeId, userId, HomeAccessRole.VIEWER);
        return repository.findAllByHome(home, pageable).map(deviceMapper::toDto);
    }

    @Transactional
    @Caching(
            evict = {@CacheEvict(value="devices", key="'list_' + #homeId + '_' + #userId")},
            put = {@CachePut(value="devices", key="'single_' + #deviceId + '_' + #userId")}
    )
    public ResponseDeviceWithSecretDto createDevice(UUID homeId, UUID userId, RequestDeviceDto dto) {
        String secret = UUID.randomUUID().toString().replace("-", "");
        Home home = findHome(homeId, userId, HomeAccessRole.MANAGER);
        Device device = deviceMapper.toEntity(dto, home);
        Device dbResult = repository.save(device);
        saveSecret(device.getDeviceId().toString(), secret);
        repository.flush();
        return deviceMapper.toDtoWithSecret(dbResult, secret);
    }

    @Transactional
    @Caching(
            evict = {@CacheEvict(value="devices", key="'list_' + #homeId + '_' + #userId")},
            put = {@CachePut(value="devices", key="'single_' + #deviceId + '_' + #userId")}
    )
    public ResponseDeviceDto updateDevice(UUID homeId, UUID userId, UUID deviceId, RequestDeviceDto dto) {
        Device device = findDevice(homeId, userId, deviceId, HomeAccessRole.MANAGER);
        deviceMapper.updateDeviceFromDto(dto, device);
        return deviceMapper.toDto(device);
    }

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value="devices", key="'list_' + #homeId + '_' + #userId"),
                    @CacheEvict(value="devices", key="'single_' + #deviceId + '_' + #userId")
            }
    )
    public void deleteDevice(UUID homeId, UUID userId, UUID deviceId) {
        Device device = findDevice(homeId, userId, deviceId, HomeAccessRole.MANAGER);
        repository.delete(device);
        return;
    }

    @Transactional
    public ResponseSecretDto refreshDeviceSecret(UUID homeId, UUID userId, UUID deviceId) {
        Device device = findDevice(homeId, userId, deviceId, HomeAccessRole.MANAGER);
        String secret = UUID.randomUUID().toString().replace("-", "");
        saveSecret(device.getDeviceId().toString(), secret);
        return new ResponseSecretDto(secret);
    }

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "devices", key = "'list_' + #homeId + '_' + #userId"),
                @CacheEvict(value = "devices", key = "'list_' + #targetHomeId + '_' + #userId"),
                @CacheEvict(value = "devices", key = "'single_' + #deviceId + '_' + #userId")
            }
    )
    public ResponseDeviceDto moveDeviceToTargetHome(UUID homeId, UUID userId, UUID deviceId, UUID targetHomeId) {
        Device device = findDevice(homeId, userId, deviceId, HomeAccessRole.MANAGER);
        Home targetHome = findHome(targetHomeId, userId, HomeAccessRole.MANAGER);
        device.setHome(targetHome);
        Device dbResult = repository.save(device);
        repository.flush();
        return deviceMapper.toDto(dbResult);
    }

    private Home findHome(UUID homeId, UUID userId, HomeAccessRole role) {
        Home home = homeManagementService.findHome(homeId, userId);
        homeManagementService.checkOwnership(home, userId, role);
        return home;
    }

    public Device findDevice(UUID homeId, UUID userId, UUID deviceId, HomeAccessRole role) {
        findHome(homeId, userId, role);
        return repository.findById(deviceId).orElseThrow(
                () -> new ObjectDoesNotExistException(
                        String.format("Device object with ID: %s does not exists", deviceId),
                        userId
                )
        );
    }

    private void saveSecret(String deviceId, String value) {
        // One device with secret -> one file.
        // We are mitigating issues with concurrent modification when all devices in one file
        // Ingestion service will easily find correct secret based on deviceId only
        try {
            vaultService.saveKeyValueSecret(deviceSecretPath + "/" + deviceId , "secret", value);
        } catch (VaultException e) {
            log.error("Error while saving device's secret: {}", e.toString());
            throw new RuntimeException("Internal server error");
        }
    }
}
