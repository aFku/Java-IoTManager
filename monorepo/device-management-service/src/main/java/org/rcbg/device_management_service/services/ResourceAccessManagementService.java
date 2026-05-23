package org.rcbg.device_management_service.services;

import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.exceptions.AccessDeniedException;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.models.entities.HomeAccess;
import org.rcbg.device_management_service.repositories.HomeAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class ResourceAccessManagementService {

    @Autowired
    private HomeAccessRepository repository;

    private HomeAccessRole findUserRoleInHome(Home home, UUID userId) {
        Optional<HomeAccess> access = repository.findByHomeAndUserId(home, userId);
        return access.map(HomeAccess::getRole).orElse(HomeAccessRole.NONE);
    }

    public boolean checkIfUserHasAccess(Home home, UUID userId, HomeAccessRole requiredRole) {
        HomeAccessRole userRole = findUserRoleInHome(home, userId);
        if (userRole == HomeAccessRole.NONE) {
            throw new ObjectDoesNotExistException("Home with ID: " + home.getHomeId() + " not found", userId);
        } else if (requiredRole.getCode() > userRole.getCode()) {
            throw new AccessDeniedException("User with ID: " + userId + " does not have minimal required role for this action: " + requiredRole, userId);
        }
        return true;
    }
}
