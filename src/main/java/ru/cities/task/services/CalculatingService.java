package ru.cities.task.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.cities.task.dto.DistanceAllResponce;
import ru.cities.task.dto.DistanceCalculationRequest;
import ru.cities.task.entity.City;
import ru.cities.task.entity.Distance;
import ru.cities.task.repositories.CityRepository;
import ru.cities.task.repositories.DistanceRepository;
import ru.cities.task.utils.CalculationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Service
public class CalculatingService {
    private static double SEMI_MAJOR_AXIS_MT = 6378137;
    private static double SEMI_MINOR_AXIS_MT = 6356752.314245;
    private static double FLATTENING = 1 / 298.257223563;
    private static double ERROR_TOLERANCE = 1e-12;
    
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private DistanceRepository distanceRepository;

    public List<DistanceAllResponce> calculateDistances(DistanceCalculationRequest request) {
        List<City> fromCities = cityRepository.findAllByNameIn(request.getFromCities());
        List<City> toCities = cityRepository.findAllByNameIn(request.getToCities());
        return switch (request.getCalculationType().ordinal()){
            case 0 -> Collections.singletonList(new DistanceAllResponce().setDistances(cycleCrow(fromCities, toCities)));
            case 1 -> Collections.singletonList(new DistanceAllResponce().setDistances(cycleMatr(fromCities, toCities)));
            case 2 -> new ArrayList<>() {{
                new DistanceAllResponce().setDistances(cycleMatr(fromCities, toCities)).setCalculationType(CalculationType.CROWFLIGHT);
                new DistanceAllResponce().setDistances(cycleMatr(fromCities, toCities)).setCalculationType(CalculationType.DISTANCE_MATRIX);
            }};
            default -> throw new IllegalStateException("Unexpected value: " + request.getCalculationType().ordinal());
        };
    }

    public List<Distance> cycleCrow(List<City> fromCities, List<City> toCities) {
        List<Distance> matrDist = new ArrayList<>();
        fromCities.forEach(f -> toCities.forEach(t -> matrDist.add(matrixOfDistances(f, t))));
        return matrDist;
    }
    public List<Distance> cycleMatr(List<City> fromCities, List<City> toCities) {
        List<Distance> matrDist = new ArrayList<>();
        fromCities.forEach(f -> toCities.forEach(t -> matrDist.add(matrixOfDistances(f, t))));
        return matrDist;
    }


    public static Double crowflight(City fromCity, City toCity) {
        double startLat = fromCity.getLatitude();
        double startLong = fromCity.getLongitude();
        double endLat = toCity.getLatitude();
        double endLong = toCity.getLongitude();

        double U1 = Math.atan((1 - FLATTENING) * Math.tan(Math.toRadians(startLat)));
        double U2 = Math.atan((1 - FLATTENING) * Math.tan(Math.toRadians(endLat)));

        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);

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

        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * Math.pow(cos2SigmaM, 2))
                - B / 6 * cos2SigmaM * (-3 + 4 * Math.pow(sinSigma, 2)) * (-3 + 4 * Math.pow(cos2SigmaM, 2))));

        double distanceMt = SEMI_MINOR_AXIS_MT * A * (sigma - deltaSigma);
        return distanceMt / 1000;
    }

    public Distance matrixOfDistances(City fromCity, City toCity) {
        Distance existDistance = distanceRepository.findByFromCityIdAndToCityId(fromCity, toCity);
        if (!Objects.isNull(existDistance))
            return existDistance;

        List<Long> allCityIds = cityRepository.findAll()
                .stream()
                .map(City::getId)
                .toList();
        List<Distance> allDistances = distanceRepository.findAll();
        int indexOfFromCity = allCityIds.indexOf(fromCity.getId());
        int indexOfToCity = allCityIds.indexOf(toCity.getId());

        int size = allDistances.size();
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
                                    .setFromCityId(fromCity.getId())
                                    .setToCityId(toCity.getId())
                                    .setDistance(distanceMatrix[i][j]);
                        }

                    }
                }
            }
        }
        return null;
    }
}


