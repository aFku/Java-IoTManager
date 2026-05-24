package org.rcbg.device_management_service.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.exceptions.AccessDeniedException;
import org.rcbg.device_management_service.exceptions.InvalidMembersRequestException;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.models.dto.home_access.*;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.models.entities.HomeAccess;
import org.rcbg.device_management_service.repositories.HomeAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResourceAccessManagementService {

    @Autowired
    private HomeAccessRepository repository;

    private HomeAccessRole findUserRoleInHome(Home home, UUID userId) {
        Optional<HomeAccess> access = repository.findByHomeAndUserId(home, userId);
        return access.map(HomeAccess::getRole).orElse(HomeAccessRole.NONE);
    }

    public void checkIfUserHasAccess(Home home, UUID userId, HomeAccessRole requiredRole) {
        HomeAccessRole userRole = findUserRoleInHome(home, userId);
        if (userRole == HomeAccessRole.NONE) {
            throw new ObjectDoesNotExistException("Home with ID: " + home.getHomeId() + " not found", userId);
        } else if (requiredRole.getCode() > userRole.getCode()) {
            throw new AccessDeniedException("User with ID: " + userId + " does not have minimal required role for this action: " + requiredRole, userId);
        }
    }

    private MembersPostResponseDto addUsersPermissions(Map<UUID, HomeAccessRole> map, Home home) {
        Set<UUID> newKeys = map.keySet();
        List<HomeAccess> existingPermissions = repository.findByHome_HomeIdAndUserIdIn(home.getHomeId(), newKeys);
        Set<UUID> existingIds = existingPermissions.stream().map(HomeAccess::getUserId).collect(Collectors.toSet());
        List<RoleUpdateResponseDto> updateResponse = new ArrayList<>();

        // Override existing permissions
        existingPermissions.forEach(access -> {
            updateResponse.add(new RoleUpdateResponseDto(
                    access.getUserId(),
                    access.getRole(),
                    map.get(access.getUserId()))
            );
            access.setRole(map.get(access.getUserId()));
        });

        repository.saveAll(existingPermissions);

        // Add not existing
        List<HomeAccess> newAccess = newKeys.stream()
                .filter(id -> !existingIds.contains(id))
                .map(id -> new HomeAccess(0, home, id, map.get(id)
                )).toList();
        List<RoleInsertResponseDto> insertResponse = newAccess.stream().map(access ->
                    new RoleInsertResponseDto(access.getUserId(), access.getRole())
                ).toList();

        repository.saveAll(newAccess);

        return new MembersPostResponseDto(insertResponse, updateResponse);
    }

    private List<UUID> removeUsersPermissions(List<UUID> users, Home home) {
        List<HomeAccess> existingPermissions = repository.findByHome_HomeIdAndUserIdIn(home.getHomeId(), new HashSet<>(users));
        repository.deleteByHomeIdAndUserIds(home.getHomeId(), existingPermissions.stream().map(HomeAccess::getUserId).toList());
        return existingPermissions.stream().map(HomeAccess::getUserId).toList();
    }

    private void checkCollidingUserIds(MembersPostRequestDto requestDto) {
        Set<UUID> addIds = requestDto.getAdd() == null ? Set.of() : requestDto.getAdd().keySet();
        List<UUID> deleteList = requestDto.getDelete() == null ? List.of() : requestDto.getDelete();

        // Check for duplicates in delete list
        Set<UUID> deleteSet = new HashSet<>(deleteList);
        if (deleteSet.size() != deleteList.size()) {
            throw new IllegalArgumentException("Duplicate UUIDs in delete list");
        }

        // Check if there is duplicated UUID between add and delete
        Set<UUID> intersection = new HashSet<>(addIds);
        intersection.retainAll(deleteSet);

        if (!intersection.isEmpty()) {
            throw new IllegalArgumentException("UUIDs present in both add and delete: " + intersection);
        }
    }

    @Transactional
    public MembersPostResponseDto handleMembersPostRequest(MembersPostRequestDto requestDto, Home home, UUID requesterId) {
        try {
            this.checkCollidingUserIds(requestDto);
        } catch (IllegalArgumentException ex) {
            throw new InvalidMembersRequestException(ex.getMessage(), requesterId);
        }
        MembersPostResponseDto response = this.addUsersPermissions(requestDto.getAdd(), home);
        log.info("User {} added {} new permissions and updated {} permissions from HomeID: {}",
                requesterId,
                response.getAdded().size(),
                response.getUpdated().size(),
                home.getHomeId());

        response.setRemoved(
                this.removeUsersPermissions(requestDto.getDelete(), home)
        );
        log.info("User {} deleted {} permissions from HomeID: {}", requesterId, response.getRemoved().size(), home.getHomeId());
        return response;
    }

    public MembersGetResponseDto getMembersByHome(Home home) {
        List<HomeAccess> result = repository.findAllByHome_HomeId(home.getHomeId());
        return new MembersGetResponseDto(
                result.stream().map(access ->
                        new RoleGetResponseDto(access.getUserId(), access.getRole()))
                        .toList()
        );
    }
}
