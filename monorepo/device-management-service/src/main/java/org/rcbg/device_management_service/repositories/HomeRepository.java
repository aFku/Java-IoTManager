package org.rcbg.device_management_service.repositories;

import org.rcbg.device_management_service.models.entities.Home;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HomeRepository extends JpaRepository<Home, UUID> {
    @Query("""
        SELECT DISTINCT home
        FROM Home home
        JOIN home.accesses access
        WHERE access.userId = :userId
        AND access.role <> org.rcbg.device_management_service.enums.HomeAccessRole.NONE
    """)
    List<Home> findAllByUserId(UUID userId);
}
