package org.rcbg.device_management_service.repositories;

import org.rcbg.device_management_service.models.entities.Home;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HomeRepository extends JpaRepository<Home, UUID> {
}
