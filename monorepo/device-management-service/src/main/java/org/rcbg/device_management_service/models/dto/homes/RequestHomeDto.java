package org.rcbg.device_management_service.models.dto.homes;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.rcbg.device_management_service.validators.groups.CreateGroup;
import org.rcbg.device_management_service.validators.groups.UpdateGroup;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestHomeDto {

    @NotEmpty(message = "Name is required", groups = CreateGroup.class)
    @Size(max = 100, min = 3, message = "Name should be between 3 and 100 characters long", groups = {
            CreateGroup.class,
            UpdateGroup.class
    })
    String name;
}
