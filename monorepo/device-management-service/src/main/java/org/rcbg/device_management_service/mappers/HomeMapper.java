package org.rcbg.device_management_service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.rcbg.device_management_service.models.dto.homes.RequestHomeDto;
import org.rcbg.device_management_service.models.dto.homes.ResponseHomeDto;
import org.rcbg.device_management_service.models.entities.Home;

@Mapper
public interface HomeMapper {

    HomeMapper INSTANCE = Mappers.getMapper(HomeMapper.class);

    public Home toEntity(RequestHomeDto dto);
    public ResponseHomeDto toDto(Home entity);
}
