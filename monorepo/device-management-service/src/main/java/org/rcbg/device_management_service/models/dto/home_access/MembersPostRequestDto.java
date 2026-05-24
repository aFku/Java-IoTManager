package org.rcbg.device_management_service.models.dto.home_access;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.rcbg.device_management_service.enums.HomeAccessRole;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MembersPostRequestDto {
    @NotNull
    public Map<UUID, HomeAccessRole> add;
    @NotNull
    public List<UUID> delete;
}
