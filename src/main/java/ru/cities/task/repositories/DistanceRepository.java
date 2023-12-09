package ru.cities.task.repositories;

import ru.cities.task.entity.City;
import ru.cities.task.entity.Distance;

import java.util.List;

public interface DistanceRepository extends AbstractRepository<Distance> {
    Distance findByFromCityIdAndToCityId(City fromCityId, City toCityId);
}
