package org.rcbg.device_management_service.controllers;

import org.rcbg.device_management_service.models.dto.devices.RequestDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceWithSecretDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseSecretDto;
import org.rcbg.device_management_service.services.DeviceManagementService;
import org.rcbg.device_management_service.validators.groups.CreateGroup;
import org.rcbg.device_management_service.validators.groups.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/homes/{homeId}/devices")
public class DevicesController {

    @Autowired
    private DeviceManagementService deviceManagementService;

    @GetMapping
    ResponseEntity<List<ResponseDeviceDto>> getAllDevicesByHomeId(@PathVariable UUID homeId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(deviceManagementService.getListOfDevices(
                        homeId,
                        UUID.randomUUID()
                ));
    }

    @PostMapping
    ResponseEntity<ResponseDeviceWithSecretDto> createNewDevice(@PathVariable UUID homeId, @Validated(CreateGroup.class) @RequestBody RequestDeviceDto requestDto) {
        ResponseDeviceWithSecretDto responseDeviceWithSecretDto = deviceManagementService.createDevice(
                homeId,
                UUID.randomUUID(),
                requestDto
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDeviceWithSecretDto);
    }

    @GetMapping("/{deviceId}")
    ResponseEntity<ResponseDeviceDto> getDeviceByDeviceId(@PathVariable UUID homeId, @PathVariable UUID deviceId) {
        ResponseDeviceDto responseDto = deviceManagementService.getDevice(
                homeId,
                UUID.randomUUID(),
                deviceId
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PatchMapping("/{deviceId}")
    ResponseEntity<ResponseDeviceDto> patchDeviceByDeviceID(@PathVariable UUID homeId, @PathVariable UUID deviceId, @Validated(UpdateGroup.class) @RequestBody RequestDeviceDto updateContent) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(deviceManagementService.updateDevice(
                        homeId,
                        UUID.randomUUID(),
                        deviceId,
                        updateContent)
                );
    }

    @DeleteMapping("/{deviceId}")
    ResponseEntity<Void> deleteDeviceByDeviceId(@PathVariable UUID homeId, @PathVariable UUID deviceId) {
        deviceManagementService.deleteDevice(
                homeId,
                UUID.randomUUID(),
                deviceId
        );
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/{deviceId}/secret")
    ResponseEntity<ResponseSecretDto> generateSecretForDeviceId(@PathVariable UUID homeId, @PathVariable UUID deviceId) {
        ResponseSecretDto responseDto = deviceManagementService.refreshDeviceSecret(
                homeId,
                UUID.randomUUID(),
                deviceId
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PutMapping("/{deviceId}/move")
    ResponseEntity<ResponseDeviceDto> moveDeviceToTargetHomeId(@PathVariable UUID homeId, @PathVariable UUID deviceId, @RequestParam(value = "target", required = true) UUID targetHomeId) {
        ResponseDeviceDto responseDto = deviceManagementService.moveDeviceToTargetHome(
                homeId,
                UUID.randomUUID(),
                deviceId,
                targetHomeId
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }
}
