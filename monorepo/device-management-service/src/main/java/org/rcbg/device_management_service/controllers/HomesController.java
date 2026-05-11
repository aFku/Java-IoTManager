package org.rcbg.device_management_service.controllers;

import org.rcbg.device_management_service.models.dto.homes.RequestHomeDto;
import org.rcbg.device_management_service.models.dto.homes.ResponseHomeDto;
import org.rcbg.device_management_service.services.HomeManagementService;
import org.rcbg.device_management_service.validators.groups.CreateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/homes")
public class HomesController {

    @Autowired
    private HomeManagementService homeManagementService;

    @GetMapping
    public List<String> getAllAvailableHomes() {
        List<String> result = new ArrayList<>();
        result.add("Not Implemented");
        return result;
    }

    @PostMapping
    public ResponseEntity<ResponseHomeDto> createNewHome(@Validated(CreateGroup.class) @RequestBody RequestHomeDto requestDto) {
        ResponseHomeDto responseDto = homeManagementService.createHome(requestDto, UUID.randomUUID());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @GetMapping("/{homeId}")
    public String getHomeByHomeId(@PathVariable String homeId) {
        return "Not Implemented";
    }

    @PatchMapping("/{homeId}")
    public String patchHomeByHomeId(@PathVariable String homeId, @RequestBody String updateContent) {
        return "Not Implemented";
    }

    @DeleteMapping("/{homeId}")
    public String deleteHomeByHomeId(@PathVariable String homeId) {
        return "Not Implemented";
    }
}
