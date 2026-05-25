package org.rcbg.device_management_service.services.jobs.utils;

import org.rcbg.device_management_service.models.dto.keycloak.UserEntryDto;
import org.rcbg.device_management_service.services.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PermissionCleanupService {

    @Autowired
    private KeycloakService keycloakService;

    public void getKeycloakUsersList(){
        List<UserEntryDto> result = keycloakService.getUsersList().block();
        result.stream().forEach(dto -> {
            System.out.println(dto.username + ":" + dto.id);
        });
    }
}
