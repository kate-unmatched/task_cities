package ru.cities.task.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.cities.task.utils.CalculationType;

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
