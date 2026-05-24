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
import org.springframework.stereotype.Service;

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

    public ResponseDeviceDto getDevice(UUID homeId, UUID userId, UUID deviceId) {
        return deviceMapper.toDto(
                findDevice(homeId, userId, deviceId, HomeAccessRole.VIEWER)
        );
    }

    // TODO: Add pagination
    public List<ResponseDeviceDto> getListOfDevices(UUID homeId, UUID userId) {
        Home home = findHome(homeId, userId, HomeAccessRole.VIEWER);
        return repository.findAllByHome(home).stream().map(deviceMapper::toDto).toList();
    }

    @Transactional
    public ResponseDeviceWithSecretDto createDevice(UUID homeId, UUID userId, RequestDeviceDto dto) {
        String secret = UUID.randomUUID().toString().replace("-", "");
        Home home = findHome(homeId, userId, HomeAccessRole.MANAGER);
        Device device = deviceMapper.toEntity(dto, secret, home);
        Device dbResult = repository.save(device);
        repository.flush();
        return deviceMapper.toDtoWithSecret(dbResult);
    }

    @Transactional
    public ResponseDeviceDto updateDevice(UUID homeId, UUID userId, UUID deviceId, RequestDeviceDto dto) {
        Device device = findDevice(homeId, userId, deviceId, HomeAccessRole.MANAGER);
        deviceMapper.updateDeviceFromDto(dto, device);
        return deviceMapper.toDto(device);
    }

    @Transactional
    public void deleteDevice(UUID homeId, UUID userId, UUID deviceId) {
        Device device = findDevice(homeId, userId, deviceId, HomeAccessRole.MANAGER);
        repository.delete(device);
        return;
    }

    @Transactional
    public ResponseSecretDto refreshDeviceSecret(UUID homeId, UUID userId, UUID deviceId) {
        Device device = findDevice(homeId, userId, deviceId, HomeAccessRole.MANAGER);
        String secret = UUID.randomUUID().toString().replace("-", "");
        device.setSecret(secret);
        Device dbResult = repository.save(device);
        return new ResponseSecretDto(dbResult.getSecret());
    }

    @Transactional
    public ResponseDeviceDto moveDeviceToTargetHome(UUID homeId, UUID userId, UUID deviceId, UUID targetHomeId) {
        Device device = findDevice(homeId, userId, deviceId, HomeAccessRole.MANAGER);
        Home targetHome = findHome(targetHomeId, userId, HomeAccessRole.MANAGER);
        device.setHome(targetHome);
        Device dbResult = repository.save(device);
        repository.flush();
        return deviceMapper.toDto(dbResult);
    }

    // TODO: Refactor ownership and device management permissions when roles are ready
    private boolean checkIfUserHasAccess(Device device, String userId) {
        return false;
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
}
