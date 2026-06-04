package org.rcbg.device_management_service.models.dto.homes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseHomeDto{
    UUID homeId;
    String name;
}
