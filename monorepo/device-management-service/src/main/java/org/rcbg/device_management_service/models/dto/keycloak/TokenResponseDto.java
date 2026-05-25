package org.rcbg.device_management_service.models.dto.keycloak;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponseDto {
    public String access_token;
    public long expires_in;
    public String token_type;
}
