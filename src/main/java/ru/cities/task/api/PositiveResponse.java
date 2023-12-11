package ru.cities.task.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.ToString;
import ru.cities.task.utils.Views;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@ToString
@JsonView(Views.AllView.class)
public class PositiveResponse<T> extends Response {

    @JsonInclude(NON_NULL)
    private final T data;

    public PositiveResponse(T data) {
        super(Boolean.TRUE);
        this.data = data;
    }
}
