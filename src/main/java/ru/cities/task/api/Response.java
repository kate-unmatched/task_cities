package ru.cities.task.api;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.cities.task.utils.Views;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@JsonView(Views.AllView.class)
public abstract class Response implements Serializable {
    private final Boolean result;
}
