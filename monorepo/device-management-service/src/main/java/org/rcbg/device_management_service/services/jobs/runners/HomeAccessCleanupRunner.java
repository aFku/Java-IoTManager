package org.rcbg.device_management_service.services.jobs.runners;

import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.services.jobs.utils.HomeAccessCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("cleanup")
public class HomeAccessCleanupRunner implements CommandLineRunner {

    @Autowired
    private HomeAccessCleanupService service;

    @Autowired
    private ConfigurableApplicationContext context;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting HomeAccess Cleanup Runner");
        int number = service.removeAbsentUuidFromAccess();
        log.info("HomeAccess Cleanup Runner removed {} records", number);
        SpringApplication.exit(context, () -> 0);
    }
}
