package org.rcbg.device_management_service.services.jobs.utils;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
public class OrphanedHomeCleanupComponent {

    @Value("${orphaned-homes.days-to-delete}")
    private int deltaDays;

    @Autowired
    private EntityManager entityManager;

    private void searchAndMarkOrphanedHomes() {
        log.info("Searching for orphaned Homes to mark");
        int updated = entityManager.createNativeQuery("""
                UPDATE homes h
                SET
                    is_orphaned = true,
                    orphaned_at = NOW()
                WHERE h.is_orphaned = false
                AND NOT EXISTS (
                    SELECT 1
                    FROM home_access ha
                    WHERE ha.home_id = h.home_id
                );
                """).executeUpdate();
        log.info("Found and marked {} new orphaned homes", updated);
    }

    private void removeOrphanedHomes() {
        log.info("Removing homes orphaned for longer than {} days", deltaDays);
        String query = String.format("""
                DELETE FROM homes
                WHERE is_orphaned = true
                AND orphaned_at < NOW() - INTERVAL '%d days';
                """, deltaDays);
        int deleted = entityManager.createNativeQuery(query).executeUpdate();
        log.info("Deleted {} orphaned homes", deleted);
    }

    private void checkIncorrectOrphanedMarkers() {
        log.info("Searching for incorrect orphan markers");
        int found = entityManager.createNativeQuery("""
                SELECT home_id
                FROM homes h
                WHERE
                    (h.is_orphaned = true AND h.orphaned_at IS NULL)
                OR
                    (h.is_orphaned = false AND h.orphaned_at IS NOT NULL)
                """).getResultList().size();
        log.info("Found {} entries with incorrect marks", found);
    }

    @Transactional
    public void startOrphanedHomeCleanup() {
        searchAndMarkOrphanedHomes();
        removeOrphanedHomes();
        checkIncorrectOrphanedMarkers();
    }
}
