package org.rcbg.device_management_service.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.mappers.HomeMapper;
import org.rcbg.device_management_service.models.dto.home_access.MembersGetResponseDto;
import org.rcbg.device_management_service.models.dto.home_access.MembersPostRequestDto;
import org.rcbg.device_management_service.models.dto.home_access.MembersPostResponseDto;
import org.rcbg.device_management_service.models.dto.homes.RequestHomeDto;
import org.rcbg.device_management_service.models.dto.homes.ResponseHomeDto;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.repositories.HomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class HomeManagementService {

    // TODO: Add more logs

    @Autowired
    private HomeRepository repository;
    @Autowired
    private HomeMapper homeMapper;
    @Autowired
    private ResourceAccessManagementService resourceAccessManagementService;

    public ResponseHomeDto getHome(UUID homeId, UUID userId) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId, HomeAccessRole.VIEWER);
        return homeMapper.toDto(home);
    }

    // TODO: Add pagination
    public List<ResponseHomeDto> getListOfHomes(UUID userId) {
        return repository.findAllByUserId(userId).stream().map(homeMapper::toDto).toList();
    }

    @Transactional
    public ResponseHomeDto createHome(RequestHomeDto dto, UUID userId) {
        log.info("Creating new home object for user: {}", userId);
        Home home = homeMapper.toEntity(dto);
        Home dbResult = repository.save(home);
        repository.flush();
        MembersPostRequestDto creatorAccessRequest = new MembersPostRequestDto();
        creatorAccessRequest.setAdd(
                Map.of(userId, HomeAccessRole.MANAGER)
        );
        resourceAccessManagementService.handleMembersPostRequest(creatorAccessRequest, home, userId);
        log.info("New home: {} created for user: {}", dbResult.getHomeId(), userId);
        return homeMapper.toDto(dbResult);
    }

    @Transactional
    public ResponseHomeDto updateHome(UUID homeId, UUID userId, RequestHomeDto dto) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId, HomeAccessRole.MANAGER);
        homeMapper.updateHomeFromDto(dto, home);
        return homeMapper.toDto(home);
    }

    @Transactional
    public void deleteHome(UUID homeId, UUID userId) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId, HomeAccessRole.MANAGER);
        repository.delete(home);
    }

    public Home findHome(UUID homeId, UUID userId) {
        return repository.findById(homeId).orElseThrow(
                () -> new ObjectDoesNotExistException(
                        String.format("Home object with ID: %s does not exists", homeId),
                        userId
                )
        );

    }

    public MembersGetResponseDto getHomeMembers(UUID homeId, UUID userId) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId, HomeAccessRole.VIEWER);
        return resourceAccessManagementService.getMembersByHome(home);
    }

    public MembersPostResponseDto updateHomeMembers(UUID homeId, UUID userId, MembersPostRequestDto requestDto) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId, HomeAccessRole.MANAGER);
        return resourceAccessManagementService.handleMembersPostRequest(requestDto, home, userId);
    }

    public void checkOwnership(Home home, UUID userId, HomeAccessRole role) {
        this.resourceAccessManagementService.checkIfUserHasAccess(home, userId, role);
    }
}
