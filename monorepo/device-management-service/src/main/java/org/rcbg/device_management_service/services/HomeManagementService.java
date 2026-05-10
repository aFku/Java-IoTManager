package org.rcbg.device_management_service.services;

import lombok.extern.slf4j.Slf4j;
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

    public void createHome() {
        return;
    }

    public void updateHome(UUID homeId) {
        return;
    }

    public void deleteHome(UUID homeId) {
        return;
    }
}
