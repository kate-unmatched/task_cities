package ru.cities.task.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;


@Data
@Entity
@EqualsAndHashCode
@Accessors(chain = true)
public class Distance {

    @Id
    @GeneratedValue(generator = "increment")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @GenericGenerator(name = "increment", strategy = "increment")
    private Long id;

    @JsonAlias("from_city_id")
    private Long fromCityId;

    @JsonAlias("to_city_id")
    private Long toCityId;

    private Double distance;

    public Distance swap(boolean swap) {
        if (swap) {
            Long tmp = fromCityId;
            fromCityId = toCityId;
            toCityId = tmp;
        }
        return this;
    }
}
