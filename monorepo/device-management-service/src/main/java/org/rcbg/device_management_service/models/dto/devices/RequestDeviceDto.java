package org.rcbg.device_management_service.models.dto.devices;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumeratedValue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.rcbg.device_management_service.enums.DeviceType;
import org.rcbg.device_management_service.validators.groups.CreateGroup;
import org.rcbg.device_management_service.validators.groups.UpdateGroup;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestDeviceDto {

    @NotEmpty(message = "Name is required" , groups = CreateGroup.class)
    @Size(max = 100, min = 3, message = "Name should be between 3 and 100 character", groups = {
            CreateGroup.class,
            UpdateGroup.class
    })
    String name;

    @NotNull(message = "Device type is required", groups = CreateGroup.class)
    // TODO: Custom message in error response if incorrect value provided in request
    DeviceType deviceType;
}
