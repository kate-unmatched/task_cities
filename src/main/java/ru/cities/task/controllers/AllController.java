package ru.cities.task.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.cities.task.api.Api;
import ru.cities.task.api.CustomPage;
import ru.cities.task.api.PositiveResponse;
import ru.cities.task.dto.DistanceAllResponse;
import ru.cities.task.dto.DistanceCalculationRequest;
import ru.cities.task.entity.City;
import ru.cities.task.entity.Distance;
import ru.cities.task.repositories.CityRepository;
import ru.cities.task.repositories.DistanceRepository;
import ru.cities.task.services.CalculatingService;
import ru.cities.task.utils.Views;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("task")
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
    public List<DistanceAllResponse> calculateDistances(@RequestBody @Valid DistanceCalculationRequest request) {
        return calculatingService.calculateDistances(request);
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping("upload") // example citiesAndDistances.xml
    public void citiesUpload(@RequestParam MultipartFile file) throws IOException {
        String ext = StringUtils.substringAfterLast(file.getOriginalFilename(), '.');
        assert "xml".equals(ext);

//        String str = StreamUtils.copyToString(file.getInputStream(), StandardCharsets.UTF_8);

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
    }
}
