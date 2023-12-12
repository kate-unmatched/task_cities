package ru.cities.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.Preconditions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import ru.cities.task.controllers.AllController;
import ru.cities.task.entity.City;
import ru.cities.task.entity.Distance;
import ru.cities.task.repositories.CityRepository;
import ru.cities.task.repositories.DistanceRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = TaskApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application.properties")
class TaskApplicationTests {
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private DistanceRepository distanceRepository;
    @InjectMocks
    private AllController allController;
    @Autowired
    private MockMvc mvc;

    @Test
    void getValidationException() throws Exception {
        mvc.perform(post("/task/calculate-distances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calculationType\": \"DISTANCE_MATRIX\",\"fromCities\": [],\"toCities\": []}"))
                .andExpect(status().is4xxClientError()); // validation for empty
    }

    @Test
    void getSuccCrow() throws Exception {

        MvcResult mvcResult = mvc.perform(post("/task/calculate-distances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calculationType\": \"CROWFLIGHT\",\"fromCities\": [1],\"toCities\": [2]}"))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(json);
        double distances = jsonNode.get("data").iterator().next().get("distances").iterator().next().get("distance").asDouble();
        double abs = Math.abs(distances - 3438.878266962273);
        double eps = 0.001;
        Preconditions.condition(abs < eps, "Successful");
    }

    @Test
    void calculateAllWayDistances() throws Exception {
        MvcResult result = mvc.perform(post("/task/calculate-distances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calculationType\": \"ALL\",\"fromCities\": 1,\"toCities\": 3}"))
                .andExpect(status().is2xxSuccessful()).andReturn();// validation for empty
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

        double crow = jsonNode.get("data").iterator().next().get("distances").iterator().next().get("distance").asDouble();
        double matrix = jsonNode.get("data").get(1).get("distances").iterator().next().get("distance").asDouble();

        Assertions.assertTrue((Math.abs(crow - 3218.592463516767)) < 0.001);
        Assertions.assertEquals(125., matrix);
    }
}
