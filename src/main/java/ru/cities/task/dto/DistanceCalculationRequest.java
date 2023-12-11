package ru.cities.task.dto;

import lombok.Data;
import ru.cities.task.utils.CalculationType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class DistanceCalculationRequest {
    @NotNull
    private CalculationType calculationType;
    @NotEmpty
    private Set<Long> fromCities;
    @NotEmpty
    private Set<Long> toCities;
}
