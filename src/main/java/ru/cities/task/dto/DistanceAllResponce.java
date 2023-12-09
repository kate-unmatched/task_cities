package ru.cities.task.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.cities.task.entity.Distance;
import ru.cities.task.utils.CalculationType;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
public class DistanceAllResponce {
    private CalculationType calculationType;
    private List<Distance> distances;
}
