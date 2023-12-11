package ru.cities.task.api;

public class Api {
    public static <T> PositiveResponse<T> positiveResponse(T data) {
        return new PositiveResponse<>(data);
    }

    public static NegativeResponse<String> negativeResponse(String code, String errorMessage, Object details) {
        return new NegativeResponse<>(code, errorMessage, details);
    }
}
