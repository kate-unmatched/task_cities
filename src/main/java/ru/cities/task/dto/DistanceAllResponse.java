package ru.cities.task.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.cities.task.entity.Distance;
import ru.cities.task.utils.CalculationType;

import java.util.List;

@Data
@Accessors(chain = true)
public class DistanceAllResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CalculationType calculationType;
    private List<Distance> distances;
}
