package ru.cities.task.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.cities.task.utils.Views;

@Data
@Entity
@EqualsAndHashCode
public class City {
    @Id
    @JsonView(Views.CityView.class)
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonView(Views.CityView.class)
    private String name;

    double latitude;
    double longitude;
}
