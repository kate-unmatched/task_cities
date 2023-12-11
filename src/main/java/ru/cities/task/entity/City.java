package ru.cities.task.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import ru.cities.task.utils.Views;

@Data
@Entity
@EqualsAndHashCode
public class City {
    @Id
    @JsonView(Views.CityView.class)
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private Long id;

    @JsonView(Views.CityView.class)
    private String name;

    Double latitude;
    Double longitude;
}
