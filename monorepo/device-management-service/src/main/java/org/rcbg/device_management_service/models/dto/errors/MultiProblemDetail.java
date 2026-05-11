package org.rcbg.device_management_service.models.dto.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MultiProblemDetail {
    String type;
    String title;
    Integer status;
    Map<String, List<String>> errors;
}
