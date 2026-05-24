package org.rcbg.device_management_service.models.dto.home_access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MembersPostResponseDto {

    public MembersPostResponseDto(List<RoleInsertResponseDto> added, List<RoleUpdateResponseDto> updated) {
        this.added = added;
        this.updated = updated;
        this.removed = new ArrayList<>();
    }

    private List<RoleInsertResponseDto> added;
    private List<RoleUpdateResponseDto> updated;
    private List<UUID> removed;
}
