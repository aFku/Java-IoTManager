package org.rcbg.device_management_service.models.dto.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StandardProblemDetail {
    String type;
    String title;
    Integer status;
    String detail;
}
