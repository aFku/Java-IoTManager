package org.rcbg.device_management_service.repositories;

import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.models.entities.HomeAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HomeAccessRepository extends JpaRepository<HomeAccess, Integer> {
    Optional<HomeAccess> findByHomeAndUserId(Home home, UUID userId);
}
