package org.rcbg.device_management_service.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.enums.DeviceManagementOperations;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.mappers.DeviceMapper;
import org.rcbg.device_management_service.models.dto.devices.RequestDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceWithSecretDto;
import org.rcbg.device_management_service.models.entities.Device;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.repositories.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class DeviceManagementService {

    @Autowired
    private DeviceRepository repository;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private HomeManagementService homeManagementService;

    public ResponseDeviceDto getDevice(UUID homeId, UUID userId, UUID deviceId) {
        return deviceMapper.toDto(
                findDevice(homeId, userId, deviceId)
        );
    }

    public void getListOfDevices(UUID homeId) {
        return;
    }

    @Transactional
    public ResponseDeviceWithSecretDto createDevice(UUID homeId, UUID userId, RequestDeviceDto dto) {
        String secret = UUID.randomUUID().toString().replace("-", "");
        Home home = findHome(homeId, userId);
        Device device = deviceMapper.toEntity(dto, secret, home);
        Device dbResult = repository.save(device);
        repository.flush();
        return deviceMapper.toDtoWithSecret(dbResult);
    }

    public void updateDevice(UUID homeId, UUID deviceId) {
        return;
    }

    public void deleteDevice(UUID homeId, UUID deviceId) {
        return;
    }

    public void refreshDeviceSecret(UUID homeId, UUID deviceId) {
        return;
    }

    public void moveDeviceToTargetHome(UUID homeId, UUID deviceId, UUID targetHomeId) {
        return;
    }

    private boolean findDeviceByHomeIdAndDeviceId(UUID homeId, UUID deviceId) {
        return false;
    }

    private boolean checkIfUserHasAccess(Device device, String userId, DeviceManagementOperations operations) {
        return false;
    }

    private Home findHome(UUID homeId, UUID userId) {
        Home home = homeManagementService.findHome(homeId, userId);
        homeManagementService.checkOwnership(home, userId);
        return home;
    }

    public Device findDevice(UUID homeId, UUID userId, UUID deviceId) {
        findHome(homeId, userId);
        return repository.findById(deviceId).orElseThrow(
                () -> new ObjectDoesNotExistException(
                        String.format("Device object with ID: %s does not exists", deviceId),
                        userId
                )
        );
    }
}
