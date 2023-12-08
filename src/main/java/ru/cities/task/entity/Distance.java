package ru.cities.task.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;


@Data
@Entity
@EqualsAndHashCode
public class Distance {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private Long id;

    //    @ManyToOne
//    @JoinColumn(name = "from_city_id", nullable = false)
    @JsonAlias("from_city_id")
    private Long fromCityId;

    //    @ManyToOne
//    @JoinColumn(name = "to_city_id", nullable = false)
    @JsonAlias("to_city_id")
    private Long toCityId;

    private Double distance;
}
