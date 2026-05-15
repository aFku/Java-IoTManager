package org.rcbg.device_management_service.controllers;

import org.rcbg.device_management_service.models.dto.devices.RequestDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceWithSecretDto;
import org.rcbg.device_management_service.services.DeviceManagementService;
import org.rcbg.device_management_service.validators.groups.CreateGroup;
import org.rcbg.device_management_service.validators.groups.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
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
    ResponseEntity<List<ResponseDeviceDto>> getAllDevicesByHomeId(@PathVariable String homeId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(deviceManagementService.getListOfDevices(UUID.fromString(homeId), UUID.randomUUID()));
    }

    @PostMapping
    ResponseEntity<ResponseDeviceWithSecretDto> createNewDevice(@PathVariable String homeId, @Validated(CreateGroup.class) @RequestBody RequestDeviceDto requestDto) {
        ResponseDeviceWithSecretDto responseDeviceWithSecretDto = deviceManagementService.createDevice(
                UUID.fromString(homeId),
                UUID.randomUUID(),
                requestDto
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDeviceWithSecretDto);
    }

    @GetMapping("/{deviceId}")
    ResponseEntity<ResponseDeviceDto> getDeviceByDeviceId(@PathVariable String homeId, @PathVariable String deviceId) {
        ResponseDeviceDto responseDto = deviceManagementService.getDevice(
                UUID.fromString(homeId),
                UUID.randomUUID(),
                UUID.fromString(deviceId)
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PatchMapping("/{deviceId}")
    ResponseEntity<ResponseDeviceDto> patchDeviceByDeviceID(@PathVariable String homeId, @PathVariable String deviceId, @Validated(UpdateGroup.class) @RequestBody RequestDeviceDto updateContent) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(deviceManagementService.updateDevice(UUID.fromString(homeId), UUID.randomUUID(), UUID.fromString(deviceId), updateContent));
    }

    @DeleteMapping("/{deviceId}")
    ResponseEntity<Void> deleteDeviceByDeviceId(@PathVariable String homeId, @PathVariable String deviceId) {
        deviceManagementService.deleteDevice(
                UUID.fromString(homeId),
                UUID.randomUUID(),
                UUID.fromString(deviceId)
        );
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/{deviceId}/secret")
    String generateSecretForDeviceId(@PathVariable String homeId, @PathVariable String deviceId) {
        return "Not Implemented";
    }

    @PostMapping("/{deviceId}/move")
    String moveDeviceToTargetHomeId(@PathVariable String homeId, @PathVariable String deviceId, @RequestBody String targetHomeId) {
        return "Not Implemented";
    }
}
