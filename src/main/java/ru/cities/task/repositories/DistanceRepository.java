package ru.cities.task.repositories;

import ru.cities.task.entity.Distance;

public interface DistanceRepository extends AbstractRepository<Distance> {
    Distance findByFromCityIdAndToCityId(Long fromCityId, Long toCityId);
}
