package org.rcbg.device_management_service.unit_tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.exceptions.AccessDeniedException;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.mappers.DeviceMapper;
import org.rcbg.device_management_service.models.dto.devices.RequestDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceWithSecretDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseSecretDto;
import org.rcbg.device_management_service.models.entities.Device;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.repositories.DeviceRepository;
import org.rcbg.device_management_service.services.DeviceManagementService;
import org.rcbg.device_management_service.services.HomeManagementService;
import org.rcbg.device_management_service.services.VaultService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceManagementServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @Mock
    private HomeManagementService homeManagementService;

    @Mock
    private VaultService vaultService;

    @InjectMocks
    private DeviceManagementService deviceManagementService;

    @Test
    void testGetDeviceSuccessful() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();
        Device device = new Device();
        ResponseDeviceDto expectedDto = new ResponseDeviceDto();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceMapper.toDto(device)).thenReturn(expectedDto);

        // WHEN
        ResponseDeviceDto result = deviceManagementService.getDevice(homeId, userId, deviceId);

        // THEN
        assertNotNull(result);
        verify(homeManagementService).checkOwnership(home, userId, HomeAccessRole.VIEWER);
    }

    @Test
    void testGetDeviceHomeDoesNotExist() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        when(homeManagementService.findHome(homeId, userId))
                .thenThrow(new ObjectDoesNotExistException("Home not found", userId));

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.getDevice(homeId, userId, deviceId),
                "Expected exception when home does not exists"
        );
        verify(deviceRepository, never()).findById(any());
    }

    @Test
    void testGetDeviceUserHasNoAccessToHome() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        doThrow(new ObjectDoesNotExistException("Object does not exists", userId))
                .when(homeManagementService).checkOwnership(home, userId, HomeAccessRole.VIEWER);

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.getDevice(homeId, userId, deviceId),
                "Expected exception when user has no access to home"
        );
        verify(deviceRepository, never()).findById(any());
    }

    @Test
    void testGetDeviceDeviceDoesNotExist() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.getDevice(homeId, userId, deviceId),
                "Expected exception when device does not exists"
        );
    }

    @Test
    void testGetListOfDevicesSuccessful() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        Device device = new Device();
        ResponseDeviceDto dto = new ResponseDeviceDto();
        Pageable pageable = PageRequest.of(0, 2);

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        when(deviceRepository.findAllByHome(home, pageable)).thenReturn(new PageImpl<>(List.of(device)));
        when(deviceMapper.toDto(device)).thenReturn(dto);

        // WHEN
        Page<ResponseDeviceDto> result = deviceManagementService.getListOfDevices(homeId, userId, pageable);

        // THEN
        assertEquals(1, result.getTotalElements());
        verify(homeManagementService).checkOwnership(home, userId, HomeAccessRole.VIEWER);
    }

    @Test
    void testGetListOfDevicesHomeNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 2);

        when(homeManagementService.findHome(homeId, userId))
                .thenThrow(new ObjectDoesNotExistException("Home not found", userId));

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.getListOfDevices(homeId, userId, pageable),
                "Expected exception when home does not exists"
        );
        verify(deviceRepository, never()).findAllByHome(any(), any());
    }

    @Test
    void testGetListOfDevicesNoAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        Pageable pageable = PageRequest.of(0, 2);

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        doThrow(new ObjectDoesNotExistException("Object", userId))
                .when(homeManagementService).checkOwnership(home, userId, HomeAccessRole.VIEWER);

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.getListOfDevices(homeId, userId, pageable),
                "Expected exception when user has no access"
        );
    }

    @Test
    void testCreateDeviceHomeNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RequestDeviceDto dto = new RequestDeviceDto();

        when(homeManagementService.findHome(homeId, userId))
                .thenThrow(new ObjectDoesNotExistException("Home not found", userId));

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.createDevice(homeId, userId, dto),
                "Expected exception when home does not exists"
        );
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void testCreateDeviceWithTooLowAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        RequestDeviceDto dto = new RequestDeviceDto();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        doThrow(new AccessDeniedException("Manager role required", userId))
                .when(homeManagementService).checkOwnership(home, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(AccessDeniedException.class, () ->
                deviceManagementService.createDevice(homeId, userId, dto),
                "Expected exception when user has to low permissions"
        );
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void testCreateDeviceSuccessful() {
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();
        RequestDeviceDto dto = new RequestDeviceDto();
        Device mockDevice = new Device();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        when(deviceMapper.toEntity(eq(dto), eq(home))).thenReturn(mockDevice);

        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device d = invocation.getArgument(0);
            d.setDeviceId(deviceId);
            return d;
        });

        // WHEN - THEN
        deviceManagementService.createDevice(homeId, userId, dto);
        verify(deviceRepository, times(1)).save(any());
        verify(vaultService, times(1))
                .saveKeyValueSecret(anyString(), anyString(), anyString());
    }

    @Test
    void testUpdateDeviceSuccessful() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();
        Device device = new Device();
        RequestDeviceDto dto = new RequestDeviceDto();
        ResponseDeviceDto expectedDto = new ResponseDeviceDto();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceMapper.toDto(device)).thenReturn(expectedDto);

        // WHEN
        ResponseDeviceDto result = deviceManagementService.updateDevice(homeId, userId, deviceId, dto);

        // THEN
        assertNotNull(result);
        verify(deviceMapper).updateDeviceFromDto(dto, device);
        verify(homeManagementService).checkOwnership(home, userId, HomeAccessRole.MANAGER);
    }

    @Test
    void testUpdateDeviceHomeNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        RequestDeviceDto dto = new RequestDeviceDto();

        when(homeManagementService.findHome(homeId, userId))
                .thenThrow(new ObjectDoesNotExistException("Home not found", userId));

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.updateDevice(homeId, userId, deviceId, dto),
                "Expected exception when home does not exists"
        );
    }

    @Test
    void testUpdateDeviceNoAccessToHome() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();
        RequestDeviceDto dto = new RequestDeviceDto();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        doThrow(new ObjectDoesNotExistException("Manager role required", userId))
                .when(homeManagementService).checkOwnership(home, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.updateDevice(homeId, userId, deviceId, dto),
                "Expected exception when users has no access to home"
        );
    }

    @Test
    void testUpdateDeviceWithTooLowAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();
        RequestDeviceDto dto = new RequestDeviceDto();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        doThrow(new AccessDeniedException("Manager role required", userId))
                .when(homeManagementService).checkOwnership(home, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(AccessDeniedException.class, () ->
                        deviceManagementService.updateDevice(homeId, userId, deviceId, dto),
                "Expected exception when users has no access to home"
        );
    }

    @Test
    void testUpdateDeviceDeviceNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();
        RequestDeviceDto dto = new RequestDeviceDto();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.updateDevice(homeId, userId, deviceId, dto),
                "Expected exception when device not found"
        );
    }

    @Test
    void testDeleteDeviceHomeNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        when(homeManagementService.findHome(homeId, userId))
                .thenThrow(new ObjectDoesNotExistException("Home not found", userId));

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.deleteDevice(homeId, userId, deviceId),
                "Expected exception when home not found"
        );
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void testDeleteDeviceNoAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        doThrow(new ObjectDoesNotExistException("Manager role required", userId))
                .when(homeManagementService).checkOwnership(home, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.deleteDevice(homeId, userId, deviceId),
                "Expected exception when user does not have access to home"
        );
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void testDeleteDeviceToLowAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        doThrow(new AccessDeniedException("Manager role required", userId))
                .when(homeManagementService).checkOwnership(home, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(AccessDeniedException.class, () ->
                deviceManagementService.deleteDevice(homeId, userId, deviceId),
                "Expected exception when user has not enough permissions"
        );
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void testDeleteDeviceDeviceNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.deleteDevice(homeId, userId, deviceId),
                "Expected exception when device does not exists"
        );
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void testRefreshDeviceSecretHomeNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        when(homeManagementService.findHome(homeId, userId))
                .thenThrow(new ObjectDoesNotExistException("Home not found", userId));

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.refreshDeviceSecret(homeId, userId, deviceId),
                "Expected exception when home does not exists"
        );
    }

    @Test
    void testRefreshDeviceSecretDeviceNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.refreshDeviceSecret(homeId, userId, deviceId),
                "Expected exception when device does not exists"
        );
    }

    @Test
    void testRefreshDeviceSecretDeviceNoAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        doThrow(new ObjectDoesNotExistException("Manager role required", userId))
                .when(homeManagementService).checkOwnership(home, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                        deviceManagementService.refreshDeviceSecret(homeId, userId, deviceId),
                "Expected exception when user does not have access to home"
        );
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void testRefreshDeviceSecretToLowAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        doThrow(new AccessDeniedException("Manager role required", userId))
                .when(homeManagementService).checkOwnership(home, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(AccessDeniedException.class, () ->
                        deviceManagementService.refreshDeviceSecret(homeId, userId, deviceId),
                "Expected exception when user has not enough permissions"
        );
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void testRefreshDeviceSecretSuccessful() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);
        Device device = new Device();
        device.setDeviceId(deviceId);

        when(homeManagementService.findHome(homeId, userId)).thenReturn(home);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        // WHEN - THEN
        deviceManagementService.refreshDeviceSecret(homeId, userId, deviceId);
        verify(vaultService, times(1))
                .saveKeyValueSecret(anyString(), anyString(), anyString());
    }

    @Test
    void testMoveDeviceToTargetHomeSourceHomeNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID targetHomeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        when(homeManagementService.findHome(homeId, userId))
                .thenThrow(new ObjectDoesNotExistException("Source home not found", userId));

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.moveDeviceToTargetHome(homeId, userId, deviceId, targetHomeId),
                "Expected exception when home does not exists"
        );
    }

    @Test
    void testMoveDeviceToTargetHomeDeviceNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID targetHomeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Home sourceHome = new Home();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(sourceHome);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.moveDeviceToTargetHome(homeId, userId, deviceId, targetHomeId),
                "Expected exception when device not found"
        );
    }

    @Test
    void testMoveDeviceToTargetHomeTargetHomeNotFound() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID targetHomeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        Home sourceHome = new Home();
        Device device = new Device();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(sourceHome);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        when(homeManagementService.findHome(targetHomeId, userId))
                .thenThrow(new ObjectDoesNotExistException("Target home not found", userId));

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                deviceManagementService.moveDeviceToTargetHome(homeId, userId, deviceId, targetHomeId),
                "Expected exception when home not found"
        );
    }

    @Test
    void testMoveDeviceToTargetHomeToLowAccessInTargetHome() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID targetHomeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        Home sourceHome = new Home();
        Home targetHome = new Home();
        Device device = new Device();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(sourceHome);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(homeManagementService.findHome(targetHomeId, userId)).thenReturn(targetHome);

        doNothing().when(homeManagementService).checkOwnership(sourceHome, userId, HomeAccessRole.MANAGER);
        doThrow(new AccessDeniedException("Manager role required for target home", userId))
                .when(homeManagementService).checkOwnership(targetHome, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(AccessDeniedException.class, () ->
                        deviceManagementService.moveDeviceToTargetHome(homeId, userId, deviceId, targetHomeId),
                "Expected exception when user has to low permissions"
        );
    }

    @Test
    void testMoveDeviceToTargetHomeToLowAccessInSourceHome() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID targetHomeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        Home sourceHome = new Home();

        when(homeManagementService.findHome(homeId, userId)).thenReturn(sourceHome);

        doThrow(new AccessDeniedException("Manager role required for source home", userId))
                .when(homeManagementService).checkOwnership(sourceHome, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(AccessDeniedException.class, () ->
                        deviceManagementService.moveDeviceToTargetHome(homeId, userId, deviceId, targetHomeId),
                "Expected exception when user has to low permissions"
        );
    }


}