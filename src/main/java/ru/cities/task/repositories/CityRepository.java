package ru.cities.task.repositories;

import ru.cities.task.entity.City;

import java.util.Collection;
import java.util.List;

public interface CityRepository extends AbstractRepository<City> {
    List<City> findAllByNameIn(Collection<String> cities);
}