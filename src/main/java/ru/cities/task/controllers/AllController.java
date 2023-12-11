package ru.cities.task.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.cities.task.api.Api;
import ru.cities.task.api.CustomPage;
import ru.cities.task.api.PositiveResponse;
import ru.cities.task.api.Response;
import ru.cities.task.dto.DistanceAllResponse;
import ru.cities.task.dto.DistanceCalculationRequest;
import ru.cities.task.entity.City;
import ru.cities.task.entity.Distance;
import ru.cities.task.repositories.CityRepository;
import ru.cities.task.repositories.DistanceRepository;
import ru.cities.task.services.CalculatingService;
import ru.cities.task.utils.Errors;
import ru.cities.task.utils.Views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("task")
@Tag(name = "Cities API", description = "Cities and calculation of distances API")
public class AllController {
    private final CityRepository cityRepository;
    private final DistanceRepository distanceRepository;
    private final CalculatingService calculatingService;
    private final ObjectMapper xmlMapper = new XmlMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    public static final ExampleMatcher MATCHER = ExampleMatcher.matching()
            .withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

    @GetMapping("cities")
    @JsonView(Views.CityView.class)
    public PositiveResponse<CustomPage<City>> getAll(@SortDefault(sort = "id", direction = Sort.Direction.ASC)
                                                     @PageableDefault(200) Pageable pageable,
                                                     @RequestParam(required = false) Map<String, Object> map) {
        Example<City> example = Example.of(xmlMapper.convertValue(map, City.class), MATCHER);
        Page<City> cityPage = cityRepository.findAll(example, pageable);
        return Api.positiveResponse(CustomPage.of(cityPage));
    }

    @PostMapping("calculate-distances")
    public PositiveResponse<List<DistanceAllResponse>> calculateDistances(@RequestBody @Valid DistanceCalculationRequest request) {
        return Api.positiveResponse(calculatingService.calculateDistances(request));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("upload") // example citiesAndDistances.xml
    public Response citiesUpload(@RequestParam MultipartFile file) throws IOException {
        String ext = StringUtils.substringAfterLast(file.getOriginalFilename(), '.');
        Errors.E101.thr(StringUtils.equals("xml",ext));

        JsonNode jsonNode = xmlMapper.readTree(file.getInputStream());

        JsonNode jsonCities = jsonNode.get("cities").get("row");
        JsonNode jsonDistances = jsonNode.get("distances").get("row");

        List<City> cities = xmlMapper.convertValue(jsonCities, xmlMapper
                .getTypeFactory().constructCollectionType(
                        ArrayList.class, City.class
                ));
        List<Distance> distances = xmlMapper.convertValue(jsonDistances, xmlMapper
                .getTypeFactory().constructCollectionType(
                        ArrayList.class, Distance.class
                ));

        // todo add validation for duplicate
        cityRepository.saveAll(cities);
        distanceRepository.saveAll(distances);
        return Api.emptyPositiveResponse();
    }
}
