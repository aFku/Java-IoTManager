package org.rcbg.device_management_service.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.mappers.HomeMapper;
import org.rcbg.device_management_service.models.dto.homes.RequestHomeDto;
import org.rcbg.device_management_service.models.dto.homes.ResponseHomeDto;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.repositories.HomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class HomeManagementService {

    @Autowired
    private HomeRepository repository;
    @Autowired
    private HomeMapper homeMapper;

    public ResponseHomeDto getHome(UUID homeId, UUID userId) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId);
        return homeMapper.toDto(home);
    }

    // TODO: Add pagination
    // TODO: Filter out homes that user is not member of.
    public List<ResponseHomeDto> getListOfHomes(UUID userId) {
        return repository.findAll().stream().map(homeMapper::toDto).toList();
    }

    @Transactional
    public ResponseHomeDto createHome(RequestHomeDto dto, UUID userId) {
        log.info("Creating new home object for user: {}", userId);
        Home home = homeMapper.toEntity(dto);
        Home dbResult = repository.save(home);
        repository.flush();
        log.info("New home: {} created for user: {}", dbResult.getHomeId(), userId);
        return homeMapper.toDto(dbResult);
    }

    @Transactional
    public ResponseHomeDto updateHome(UUID homeId, UUID userId, RequestHomeDto dto) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId);
        homeMapper.updateHomeFromDto(dto, home);
        return homeMapper.toDto(home);
    }

    @Transactional
    public void deleteHome(UUID homeId, UUID userId) {
        Home home = findHome(homeId, userId);
        checkOwnership(home, userId);
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

    public void checkOwnership(Home home, UUID userId) {
        // TODO: Implement when roles will be ready
        return;
    }

    public void checkRequiredRole(Home home, UUID userId) {
        // TODO: Implement when roles will be ready
    }
}
