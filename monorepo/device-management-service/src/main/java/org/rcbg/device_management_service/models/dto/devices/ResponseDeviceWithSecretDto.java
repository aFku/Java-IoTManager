package org.rcbg.device_management_service.models.dto.devices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDeviceWithSecretDto extends ResponseDeviceDto{
    String secret;
}
