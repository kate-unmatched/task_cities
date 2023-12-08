package ru.cities.task.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CalculationType {
    CROWFLIGHT,
    DISTANCE_MATRIX,
    ALL
}