package org.rcbg.device_management_service.services;

import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.enums.DeviceManagementOperations;
import org.rcbg.device_management_service.mappers.DeviceMapper;
import org.rcbg.device_management_service.models.entities.Device;
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

    public void getDevice(UUID homeId, UUID deviceId) {
        return;
    }

    public void getListOfDevices(UUID homeId) {
        return;
    }

    public void createDevice(UUID homeId) {
        return;
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
}
