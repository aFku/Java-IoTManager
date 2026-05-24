package org.rcbg.device_management_service.repositories;

import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.models.entities.HomeAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface HomeAccessRepository extends JpaRepository<HomeAccess, Integer> {
    Optional<HomeAccess> findByHomeAndUserId(Home home, UUID userId);
    List<HomeAccess> findByHome_HomeIdAndUserIdIn(UUID homeId, Set<UUID> users);
    List<HomeAccess> findAllByHomeId(UUID homeId);

    @Modifying
    @Query("""
        DELETE FROM HomeAccess access
        WHERE access.home.id = :homeId
        AND access.userId in :users
    """)
    int deleteByHomeIdAndUserIds(
            @Param("homeId") UUID homeId,
            @Param("users") List<UUID> users
    );
}
