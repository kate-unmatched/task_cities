package ru.cities.task.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
@Slf4j
@Getter
@RequiredArgsConstructor
public enum Errors {
    E707("Отсутствуют обязательные параметры: %s"),
    E101("Неподдерживаемый формат файла"),
    E003("Системная ошибка");

    private final String description;

    public CodifiedException thr(Object... args) {
        return new CodifiedException(this, String.format(this.description, args));
    }

    public void thr(Boolean isTrue, Object... args) {
        if (Boolean.TRUE.equals(isTrue)) return;
        throw new CodifiedException(this, String.format(this.description, args));
    }


    @Getter
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class CodifiedException extends RuntimeException {
        private final Errors error;
        private String msg;

        public String getMsg() {
            return StringUtils.defaultString(msg, error.getDescription());
        }
    }
}
