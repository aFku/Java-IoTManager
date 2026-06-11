package org.rcbg.device_management_service.controllers;

import org.rcbg.device_management_service.models.dto.devices.RequestDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceWithSecretDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseSecretDto;
import org.rcbg.device_management_service.services.DeviceManagementService;
import org.rcbg.device_management_service.validators.groups.CreateGroup;
import org.rcbg.device_management_service.validators.groups.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/homes/{homeId}/devices")
public class DevicesController {

    @Autowired
    private DeviceManagementService deviceManagementService;

    @GetMapping
    @PreAuthorize("hasRole('DEVICE_READ')")
    ResponseEntity<Page<ResponseDeviceDto>> getAllDevicesByHomeId(@PathVariable UUID homeId, Authentication auth, @PageableDefault(page = 0, size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(deviceManagementService.getListOfDevices(
                        homeId,
                        UUID.fromString(auth.getName()),
                        pageable
                ));
    }

    @PostMapping
    @PreAuthorize("hasRole('DEVICE_WRITE')")
    ResponseEntity<ResponseDeviceWithSecretDto> createNewDevice(@PathVariable UUID homeId, @Validated(CreateGroup.class) @RequestBody RequestDeviceDto requestDto, Authentication auth) {
        ResponseDeviceWithSecretDto responseDeviceWithSecretDto = deviceManagementService.createDevice(
                homeId,
                UUID.fromString(auth.getName()),
                requestDto
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDeviceWithSecretDto);
    }

    @GetMapping("/{deviceId}")
    @PreAuthorize("hasRole('DEVICE_READ')")
    ResponseEntity<ResponseDeviceDto> getDeviceByDeviceId(@PathVariable UUID homeId, @PathVariable UUID deviceId, Authentication auth) {
        ResponseDeviceDto responseDto = deviceManagementService.getDevice(
                homeId,
                UUID.fromString(auth.getName()),
                deviceId
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PatchMapping("/{deviceId}")
    @PreAuthorize("hasRole('DEVICE_WRITE')")
    ResponseEntity<ResponseDeviceDto> patchDeviceByDeviceID(@PathVariable UUID homeId, @PathVariable UUID deviceId, @Validated(UpdateGroup.class) @RequestBody RequestDeviceDto updateContent, Authentication auth) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(deviceManagementService.updateDevice(
                        homeId,
                        UUID.fromString(auth.getName()),
                        deviceId,
                        updateContent)
                );
    }

    @DeleteMapping("/{deviceId}")
    @PreAuthorize("hasRole('DEVICE_WRITE')")
    ResponseEntity<Void> deleteDeviceByDeviceId(@PathVariable UUID homeId, @PathVariable UUID deviceId, Authentication auth) {
        deviceManagementService.deleteDevice(
                homeId,
                UUID.fromString(auth.getName()),
                deviceId
        );
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/{deviceId}/secret")
    @PreAuthorize("hasRole('DEVICE_WRITE')")
    ResponseEntity<ResponseSecretDto> generateSecretForDeviceId(@PathVariable UUID homeId, @PathVariable UUID deviceId, Authentication auth) {
        ResponseSecretDto responseDto = deviceManagementService.refreshDeviceSecret(
                homeId,
                UUID.fromString(auth.getName()),
                deviceId
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PutMapping("/{deviceId}/move")
    @PreAuthorize("hasRole('DEVICE_WRITE')")
    ResponseEntity<ResponseDeviceDto> moveDeviceToTargetHomeId(@PathVariable UUID homeId, @PathVariable UUID deviceId, @RequestParam(value = "target", required = true) UUID targetHomeId, Authentication auth) {
        ResponseDeviceDto responseDto = deviceManagementService.moveDeviceToTargetHome(
                homeId,
                UUID.fromString(auth.getName()),
                deviceId,
                targetHomeId
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }
}
