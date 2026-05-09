package org.rcbg.device_management_service.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/homes/{homeId}/devices")
public class DevicesController {

    @GetMapping
    List<String> getAllDevicesByHomeId(@PathVariable String homeId) {
        List<String> result = new ArrayList<>();
        result.add("Not Implemented");
        return result;
    }

    @PostMapping
    String createNewDevice(@PathVariable String homeId) {
        return "Not Implemented";
    }

    @GetMapping("/{deviceId}")
    String getDeviceByDeviceId(@PathVariable String homeId, @PathVariable String deviceId) {
        return "Not Implemented";
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
