package org.rcbg.device_management_service.models.dto.home_access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MembersGetResponseDto {
    private List<RoleGetResponseDto> members;
}
