package org.rcbg.device_management_service.models.dto.devices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.rcbg.device_management_service.enums.DeviceType;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDeviceDto {
    UUID deviceId;
    String name;
    DeviceType deviceType;
    UUID homeId;
}
