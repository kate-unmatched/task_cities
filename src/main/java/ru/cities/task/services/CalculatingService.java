package ru.cities.task.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.cities.task.dto.DistanceCalculationRequest;
import ru.cities.task.entity.City;
import ru.cities.task.entity.Distance;
import ru.cities.task.repositories.CityRepository;
import ru.cities.task.repositories.DistanceRepository;

import java.util.Collections;
import java.util.List;

@Service
public class CalculatingService {
    private static int EARTH_RADIUS = 1;

    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private DistanceRepository distanceRepository;

    public List<Distance> calculateDistances(DistanceCalculationRequest request) {
        List<City> fromCities = cityRepository.findAllByNameIn(request.getFromCities());
        List<City> toCities = cityRepository.findAllByNameIn(request.getToCities());

        List<Distance> distances = Collections.emptyList();

        return distances;
    }

    public Double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
//        double startLat = fromCity
//        double startLong = fromCity
//        double endLat = toCity
//        double endLong = toCity
        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
