package ru.cities.task.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.cities.task.dto.DistanceAllResponse;
import ru.cities.task.dto.DistanceCalculationRequest;
import ru.cities.task.entity.City;
import ru.cities.task.entity.Distance;
import ru.cities.task.repositories.CityRepository;
import ru.cities.task.repositories.DistanceRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static ru.cities.task.utils.CalculationType.CROWFLIGHT;
import static ru.cities.task.utils.CalculationType.DISTANCE_MATRIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatingService {
    private static final double SEMI_MAJOR_AXIS_MT = 6378137;
    private static final double SEMI_MINOR_AXIS_MT = 6356752.314245;
    private static final double FLATTENING = 1 / 298.257223563;
    private static final double ERROR_TOLERANCE = 1e-12;

    private final CityRepository cityRepository;
    private final DistanceRepository distanceRepository;

    public List<DistanceAllResponse> calculateDistances(DistanceCalculationRequest request) {
        log.info("Start of the calculator service");
        List<City> fromCities = cityRepository.findAllById(request.getFromCities());
        List<City> toCities = cityRepository.findAllById(request.getToCities());
        return switch (request.getCalculationType()) {
            case CROWFLIGHT ->
                    Collections.singletonList(new DistanceAllResponse().setDistances(cycleCrow(fromCities, toCities)));
            case DISTANCE_MATRIX ->
                    Collections.singletonList(new DistanceAllResponse().setDistances(cycleMatr(fromCities, toCities)));
            case ALL -> Arrays.asList(
                    new DistanceAllResponse().setDistances(cycleCrow(fromCities, toCities)).setCalculationType(CROWFLIGHT),
                    new DistanceAllResponse().setDistances(cycleMatr(fromCities, toCities)).setCalculationType(DISTANCE_MATRIX)
            );
        };
    }

    public List<Distance> cycleCrow(List<City> fromCities, List<City> toCities) {
        log.info("Start cycle of CROWFLIGHT");
        return fromCities.stream().flatMap(from -> toCities
                        .stream().map(to -> crowflight(from, to)))
                .toList();
    }

    public List<Distance> cycleMatr(List<City> fromCities, List<City> toCities) {
        log.info("Start cycle of DISTANCE_MATRIX");
        return fromCities.stream().flatMap(from -> toCities
                        .stream().map(to -> matrixOfDistances(from, to)))
                .toList();
    }


    public Distance crowflight(City fromCity, City toCity) {
        double startLat = fromCity.getLatitude();
        double startLong = fromCity.getLongitude();
        double endLat = toCity.getLatitude();
        double endLong = toCity.getLongitude();

        double u1 = Math.atan((1 - FLATTENING) * Math.tan(Math.toRadians(startLat)));
        double u2 = Math.atan((1 - FLATTENING) * Math.tan(Math.toRadians(endLat)));

        double sinU1 = Math.sin(u1);
        double cosU1 = Math.cos(u1);
        double sinU2 = Math.sin(u2);
        double cosU2 = Math.cos(u2);

        double longitudeDifference = Math.toRadians(endLong - startLong);
        double previousLongitudeDifference;

        double sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;

        do {
            sinSigma = Math.sqrt(Math.pow(cosU2 * Math.sin(longitudeDifference), 2) +
                    Math.pow(cosU1 * sinU2 - sinU1 * cosU2 * Math.cos(longitudeDifference), 2));
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * Math.cos(longitudeDifference);
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * Math.sin(longitudeDifference) / sinSigma;
            cosSqAlpha = 1 - Math.pow(sinAlpha, 2);
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) {
                cos2SigmaM = 0;
            }
            previousLongitudeDifference = longitudeDifference;
            double C = FLATTENING / 16 * cosSqAlpha * (4 + FLATTENING * (4 - 3 * cosSqAlpha));
            longitudeDifference = Math.toRadians(endLong - startLong) + (1 - C) * FLATTENING * sinAlpha *
                    (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * Math.pow(cos2SigmaM, 2))));
        } while (Math.abs(longitudeDifference - previousLongitudeDifference) > ERROR_TOLERANCE);

        double uSq = cosSqAlpha * (Math.pow(SEMI_MAJOR_AXIS_MT, 2) - Math.pow(SEMI_MINOR_AXIS_MT, 2)) / Math.pow(SEMI_MINOR_AXIS_MT, 2);

        double a = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double b = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

        double deltaSigma = b * sinSigma * (cos2SigmaM + b / 4 * (cosSigma * (-1 + 2 * Math.pow(cos2SigmaM, 2))
                - b / 6 * cos2SigmaM * (-3 + 4 * Math.pow(sinSigma, 2)) * (-3 + 4 * Math.pow(cos2SigmaM, 2))));

        double distanceMt = SEMI_MINOR_AXIS_MT * a * (sigma - deltaSigma);
        return new Distance()
                .setFromCityId(fromCity.getId())
                .setToCityId(toCity.getId())
                .setDistance(distanceMt / 1000);
    }

    public Distance matrixOfDistances(City fromCity, City toCity) {
        Long fromCityId = fromCity.getId();
        Long toCityId = toCity.getId();
        if (Objects.equals(fromCityId, toCityId))
            return new Distance()
                    .setFromCityId(fromCityId)
                    .setToCityId(toCityId)
                    .setDistance(.0);

        boolean swap;
        if (swap = toCityId < fromCityId) {
            Long tmp = fromCityId;
            fromCityId = toCityId;
            toCityId = tmp;
        }

        Distance existDistance = distanceRepository.findByFromCityIdAndToCityId(fromCityId, toCityId);
        if (Objects.nonNull(existDistance))
            return existDistance.swap(swap);


        List<Long> allCityIds = cityRepository.findAllId();
        List<Distance> allDistances = distanceRepository.findAll();
        int indexOfFromCity = allCityIds.indexOf(fromCityId);
        int indexOfToCity = allCityIds.indexOf(toCityId);

        int size = allCityIds.size();
        double[][] distanceMatrix = new double[size][size];

        // Инициализация матрицы смежности
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                } else {
                    distanceMatrix[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }

        // Заполнение матрицы смежности расстояниями из базы данных
        for (Distance distance : allDistances) {
            int fromIndex = allCityIds.indexOf(distance.getFromCityId());
            int toIndex = allCityIds.indexOf(distance.getToCityId());
            distanceMatrix[fromIndex][toIndex] = distance.getDistance();
        }

        // Алгоритм Флойда-Уоршелла
        for (int k = 0; k < size; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (distanceMatrix[i][k] + distanceMatrix[k][j] < distanceMatrix[i][j]) {
                        distanceMatrix[i][j] = distanceMatrix[i][k] + distanceMatrix[k][j];
                        if (i == indexOfFromCity && j == indexOfToCity || i == indexOfToCity && j == indexOfFromCity) {
                            return new Distance()
                                    .setFromCityId(fromCityId)
                                    .setToCityId(toCityId)
                                    .setDistance(distanceMatrix[i][j])
                                    .swap(swap);
                        }

                    }
                }
            }
        }
        return null;
    }
}


