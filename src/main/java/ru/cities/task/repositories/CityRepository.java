package ru.cities.task.repositories;

import org.springframework.data.jpa.repository.Query;
import ru.cities.task.entity.City;

import java.util.List;

public interface CityRepository extends AbstractRepository<City> {
    @Query("select c.id from City c")
    List<Long> findAllId();
}