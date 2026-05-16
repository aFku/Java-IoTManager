package org.rcbg.device_management_service.controllers;

import org.rcbg.device_management_service.models.dto.homes.RequestHomeDto;
import org.rcbg.device_management_service.models.dto.homes.ResponseHomeDto;
import org.rcbg.device_management_service.services.HomeManagementService;
import org.rcbg.device_management_service.validators.groups.CreateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/homes")
public class HomesController {

    @Autowired
    private HomeManagementService homeManagementService;

    @GetMapping
    public ResponseEntity<List<ResponseHomeDto>> getAllAvailableHomes() {
        List<ResponseHomeDto> responseDtoList = homeManagementService.getListOfHomes(UUID.randomUUID());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDtoList);
    }

    // TODO: Add userId parameter once keycloak is setup
    @PostMapping
    public ResponseEntity<ResponseHomeDto> createNewHome(@Validated(CreateGroup.class) @RequestBody RequestHomeDto requestDto) {
        ResponseHomeDto responseDto = homeManagementService.createHome(
                requestDto,
                UUID.randomUUID()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @GetMapping("/{homeId}")
    public ResponseEntity<ResponseHomeDto> getHomeByHomeId(@PathVariable UUID homeId) {
        ResponseHomeDto responseDto = homeManagementService.getHome(
                homeId,
                UUID.randomUUID()
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PatchMapping("/{homeId}")
    public ResponseEntity<ResponseHomeDto> patchHomeByHomeId(@PathVariable UUID homeId, @RequestBody RequestHomeDto updateContent) {
        ResponseHomeDto responseHomeDto = homeManagementService.updateHome(
                homeId,
                UUID.randomUUID(),
                updateContent);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseHomeDto);

    }

    @DeleteMapping("/{homeId}")
    public ResponseEntity<Void> deleteHomeByHomeId(@PathVariable UUID homeId) {
        homeManagementService.deleteHome(
                homeId,
                UUID.randomUUID()
        );
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
