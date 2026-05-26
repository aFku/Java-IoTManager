package org.rcbg.device_management_service.services.jobs.utils;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.rcbg.device_management_service.models.dto.keycloak.UserEntryDto;
import org.rcbg.device_management_service.services.HomeManagementService;
import org.rcbg.device_management_service.services.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HomeAccessCleanupService {

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private EntityManager entityManager;

    private List<UUID> getKeycloakUsersList(){
        List<UserEntryDto> result = keycloakService.getUsersList().block();
        return result.stream().map(entry -> UUID.fromString(entry.getId())).collect(Collectors.toList());
    }

    private void prepareTemporaryTable(List<UUID> users) {
        log.info("Preparing temporary table for cleanup with {} ids", users.size());
        entityManager.createNativeQuery("""
                CREATE TEMP TABLE cleanup_ids (
                    id uuid PRIMARY KEY
                ) ON COMMIT DROP
                """).executeUpdate();
        Session session = entityManager.unwrap(Session.class);

        session.doWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO cleanup_ids(id) VALUES (?)")) {
                int batchSize = 10000;
                int i = 0;
                for (UUID id : users) {
                    ps.setObject(1, id);
                    ps.addBatch();
                    if (++i % batchSize == 0) {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();
            }
        });
        log.info("Table prepared");
    }

    private int deleteAccessWithTemporaryTable() {
        log.info("Removing HomeAccess with absent UUID");
        int number = entityManager.createNativeQuery("""
            DELETE FROM home_access i
            WHERE NOT EXISTS (
                SELECT 1
                FROM cleanup_ids k
                WHERE k.id = i.user_id
            )
        """).executeUpdate();
        log.info("Removal process finished");
        return number;
    }

    @Transactional
    public int removeAbsentUuidFromAccess() {
        List<UUID> users = getKeycloakUsersList();
        prepareTemporaryTable(users);
        return deleteAccessWithTemporaryTable();
    }
}
