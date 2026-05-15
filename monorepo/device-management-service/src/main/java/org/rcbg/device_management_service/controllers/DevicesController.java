package org.rcbg.device_management_service.controllers;

import org.rcbg.device_management_service.models.dto.devices.RequestDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceWithSecretDto;
import org.rcbg.device_management_service.services.DeviceManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    List<String> getAllDevicesByHomeId(@PathVariable String homeId) {
        List<String> result = new ArrayList<>();
        result.add("Not Implemented");
        return result;
    }

    @PostMapping
    ResponseEntity<ResponseDeviceWithSecretDto> createNewDevice(@PathVariable String homeId, @RequestBody RequestDeviceDto requestDto) {
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
    String patchDeviceByDeviceID(@PathVariable String homeId, @PathVariable String deviceId, @RequestBody String updateContent) {
        return "Not Implemented";
    }

    @DeleteMapping("/{deviceId}")
    String deleteDeviceByDeviceId(@PathVariable String homeId, @PathVariable String deviceId) {
        return "Not Implemented";
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
