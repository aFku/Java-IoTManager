package org.rcbg.device_management_service.controllers;

import org.rcbg.device_management_service.models.dto.home_access.MembersPostRequestDto;
import org.rcbg.device_management_service.models.dto.home_access.MembersPostResponseDto;
import org.rcbg.device_management_service.models.dto.home_access.RoleGetResponseDto;
import org.rcbg.device_management_service.models.dto.homes.RequestHomeDto;
import org.rcbg.device_management_service.models.dto.homes.ResponseHomeDto;
import org.rcbg.device_management_service.services.HomeManagementService;
import org.rcbg.device_management_service.validators.groups.CreateGroup;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/homes")
public class HomesController {

    @Autowired
    private HomeManagementService homeManagementService;

    @GetMapping
    @PreAuthorize("hasRole('HOME_READ')")
    public ResponseEntity<Page<ResponseHomeDto>> getAllAvailableHomes(Authentication auth, @PageableDefault(page = 0, size = 20, sort = "name") Pageable pageable) {
        Page<ResponseHomeDto> responseDtoList = homeManagementService.getListOfHomes(UUID.fromString(auth.getName()), pageable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDtoList);
    }

    @PostMapping
    @PreAuthorize("hasRole('HOME_WRITE')")
    public ResponseEntity<ResponseHomeDto> createNewHome(@Validated(CreateGroup.class) @RequestBody RequestHomeDto requestDto, Authentication auth) {
        ResponseHomeDto responseDto = homeManagementService.createHome(
                requestDto,
                UUID.fromString(auth.getName())
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @GetMapping("/{homeId}")
    @PreAuthorize("hasRole('HOME_READ')")
    public ResponseEntity<ResponseHomeDto> getHomeByHomeId(@PathVariable UUID homeId, Authentication auth) {
        ResponseHomeDto responseDto = homeManagementService.getHome(
                homeId,
                UUID.fromString(auth.getName())
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PatchMapping("/{homeId}")
    @PreAuthorize("hasRole('HOME_WRITE')")
    public ResponseEntity<ResponseHomeDto> patchHomeByHomeId(@PathVariable UUID homeId, @RequestBody RequestHomeDto updateContent, Authentication auth) {
        ResponseHomeDto responseHomeDto = homeManagementService.updateHome(
                homeId,
                UUID.fromString(auth.getName()),
                updateContent);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseHomeDto);

    }

    @DeleteMapping("/{homeId}")
    @PreAuthorize("hasRole('HOME_WRITE')")
    public ResponseEntity<Void> deleteHomeByHomeId(@PathVariable UUID homeId, Authentication auth) {
        homeManagementService.deleteHome(
                homeId,
                UUID.fromString(auth.getName())
        );
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{homeId}/members")
    @PreAuthorize("hasRole('HOME_READ')")
    public ResponseEntity<Page<RoleGetResponseDto>> getMembersList(@PathVariable UUID homeId, Authentication auth, @PageableDefault(page = 0, size = 20, sort = "userId") Pageable pageable) {
        Page<RoleGetResponseDto> responseDto = homeManagementService.getHomeMembers(homeId, UUID.fromString(auth.getName()), pageable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PostMapping("/{homeId}/members")
    @PreAuthorize("hasRole('HOME_WRITE')")
    public ResponseEntity<MembersPostResponseDto> updateMembersList(@PathVariable UUID homeId, @RequestBody MembersPostRequestDto requestDto, Authentication auth) {
        MembersPostResponseDto responseDto = homeManagementService.updateHomeMembers(homeId, UUID.fromString(auth.getName()), requestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }
}
