package org.rcbg.device_management_service.services.jobs.runners;

import org.rcbg.device_management_service.services.jobs.utils.PermissionCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("cleanup")
public class PermissionCleanupRunner implements CommandLineRunner {

    @Autowired
    private PermissionCleanupService service;

    @Autowired
    private ConfigurableApplicationContext context;

    @Override
    public void run(String... args) throws Exception {
        service.getKeycloakUsersList();
        SpringApplication.exit(context, () -> 0);
    }
}
