package org.rcbg.device_management_service.services.jobs.runners;

import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.services.jobs.utils.HomeAccessCleanupComponent;
import org.rcbg.device_management_service.services.jobs.utils.OrphanedHomeCleanupComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("orphans_cleanup")
public class OrphanedHomeCleanupRunner implements CommandLineRunner {

    @Autowired
    private OrphanedHomeCleanupComponent component;

    @Autowired
    private ConfigurableApplicationContext context;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Orphaned Home Cleanup Runner");
        component.startOrphanedHomeCleanup();
        log.info("Finishing Orphaned Home Cleanup Runner");
        SpringApplication.exit(context, () -> 0);
    }
}
