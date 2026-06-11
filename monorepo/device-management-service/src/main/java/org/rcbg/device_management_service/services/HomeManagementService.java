package org.rcbg.device_management_service.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.mappers.HomeMapper;
import org.rcbg.device_management_service.models.dto.home_access.MembersPostRequestDto;
import org.rcbg.device_management_service.models.dto.home_access.MembersPostResponseDto;
import org.rcbg.device_management_service.models.dto.home_access.RoleGetResponseDto;
import org.rcbg.device_management_service.models.dto.homes.RequestHomeDto;
import org.rcbg.device_management_service.models.dto.homes.ResponseHomeDto;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.repositories.HomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Cacheable(value="homes", key="'single_' + #homeId + '_' + #userId")
    public ResponseHomeDto getHome(UUID homeId, UUID userId) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId, HomeAccessRole.VIEWER);
        return homeMapper.toDto(home);
    }

    @Cacheable(value="homes", key="'list_' + #userId + '_pageNumber_' + #pageable.pageNumber + '_pageSize_' + #pageable.pageSize + '_sort_' + #pageable.sort.toString()")
    public Page<ResponseHomeDto> getListOfHomes(UUID userId, Pageable pageable) {
        return repository.findAllByUserId(userId, pageable).map(homeMapper::toDto);
    }

    @Transactional
    @Caching(
            evict = {@CacheEvict(value="homes", key="'list_' + #userId")},
            put = {@CachePut(value="homes", key="'single_' + #homeId + '_' + #userId")}
    )
    public ResponseHomeDto createHome(RequestHomeDto dto, UUID userId) {
        log.info("Creating new home object for user: {}", userId);
        Home home = homeMapper.toEntity(dto);
        Home dbResult = repository.save(home);
        repository.flush();
        MembersPostRequestDto creatorAccessRequest = new MembersPostRequestDto();
        creatorAccessRequest.setAdd(
                Map.of(userId, HomeAccessRole.MANAGER)
        );
        creatorAccessRequest.setDelete(new LinkedList<>());
        resourceAccessManagementService.handleMembersPostRequest(creatorAccessRequest, dbResult, userId);
        log.info("New home: {} created for user: {}", dbResult.getHomeId(), userId);
        return homeMapper.toDto(dbResult);
    }

    @Transactional
    @Caching(
            evict = {@CacheEvict(value="homes", key="'list_' + #userId")},
            put = {@CachePut(value="homes", key="'single_' + #homeId + '_' + #userId")}
    )
    public ResponseHomeDto updateHome(UUID homeId, UUID userId, RequestHomeDto dto) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId, HomeAccessRole.MANAGER);
        homeMapper.updateHomeFromDto(dto, home);
        return homeMapper.toDto(home);
    }

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value="homes", key="'list_' + #userId"),
                    @CacheEvict(value="homes", key="'single_' + #homeId + '_' + #userId")
            }
    )
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

    @Cacheable(value="homes_members", key="#homeId + '_' + #userId + '_pageNumber_' + #pageable.pageNumber + '_pageSize_' + #pageable.pageSize + '_sort_' + #pageable.sort.toString()")
    public Page<RoleGetResponseDto> getHomeMembers(UUID homeId, UUID userId, Pageable pageable) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId, HomeAccessRole.VIEWER);
        return resourceAccessManagementService.getMembersByHome(home, pageable);
    }

    @Caching(
            evict = {@CacheEvict(value="homes_members", allEntries = true)}
    )
    public MembersPostResponseDto updateHomeMembers(UUID homeId, UUID userId, MembersPostRequestDto requestDto) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId, HomeAccessRole.MANAGER);
        return resourceAccessManagementService.handleMembersPostRequest(requestDto, home, userId);
    }

    public void checkOwnership(Home home, UUID userId, HomeAccessRole role) {
        this.resourceAccessManagementService.checkIfUserHasAccess(home, userId, role);
    }
}
