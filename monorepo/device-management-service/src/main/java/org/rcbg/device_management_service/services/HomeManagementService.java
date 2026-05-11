package org.rcbg.device_management_service.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.mappers.HomeMapper;
import org.rcbg.device_management_service.models.dto.homes.RequestHomeDto;
import org.rcbg.device_management_service.models.dto.homes.ResponseHomeDto;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.repositories.HomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class HomeManagementService {

    @Autowired
    private HomeRepository repository;

    public void getHome(UUID homeId) {
        return;
    }

    public void getListOfHomes() {
        return;
    }

    @Transactional
    public ResponseHomeDto createHome(RequestHomeDto dto, UUID userId) {
        log.info("Creating new home object for user: {}", userId);
        Home home = HomeMapper.INSTANCE.toEntity(dto);
        Home dbResult = repository.save(home);
        repository.flush();
        log.info("New home: {} created for user: {}", dbResult.getHomeId(), userId);
        return HomeMapper.INSTANCE.toDto(dbResult);
    }

    public void updateHome(UUID homeId) {
        return;
    }

    public void deleteHome(UUID homeId) {
        return;
    }
}
