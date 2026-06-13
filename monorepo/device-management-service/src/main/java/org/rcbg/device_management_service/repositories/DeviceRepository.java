package org.rcbg.device_management_service.repositories;

import org.rcbg.device_management_service.models.entities.Device;
import org.rcbg.device_management_service.models.entities.Home;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Page<Device> findAllByHome(Home home, Pageable pageable);

    @Query("SELECT d.deviceId FROM Device d WHERE d.deviceId IN :ids")
    Set<UUID> findExistingDeviceIds(@Param("ids") List<UUID> ids);
}
