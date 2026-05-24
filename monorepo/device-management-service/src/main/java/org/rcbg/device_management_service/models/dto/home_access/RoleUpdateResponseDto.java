package org.rcbg.device_management_service.models.dto.home_access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.rcbg.device_management_service.enums.HomeAccessRole;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateResponseDto {
    private UUID userId;
    private HomeAccessRole previous;
    private HomeAccessRole current;
}
