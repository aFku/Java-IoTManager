package org.rcbg.device_management_service.unit_tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rcbg.device_management_service.repositories.HomeRepository;
import org.rcbg.device_management_service.services.HomeManagementService;

@ExtendWith(MockitoExtension.class)
public class HomeManagementServiceTest {

    @Mock
    private HomeRepository repository;

    @InjectMocks
    private HomeManagementService homeManagementService;


}
