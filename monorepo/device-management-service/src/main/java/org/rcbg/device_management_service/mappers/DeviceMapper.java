package org.rcbg.device_management_service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.rcbg.device_management_service.models.dto.devices.RequestDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceDto;
import org.rcbg.device_management_service.models.entities.Device;
import org.rcbg.device_management_service.models.entities.Home;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DeviceMapper {

    @Mapping(source = "device.home.homeId", target = "homeId")
    public ResponseDeviceDto toDto(Device device);

    @Mapping(target = "home", source = "home")
    @Mapping(target = "secret", source = "secret")
    public Device toEntity(RequestDeviceDto dto, String secret, Home home);

    public void updateDeviceFromDto(RequestDeviceDto dto, @MappingTarget Device device);
}
