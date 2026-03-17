package com.cartaxi.common.util;

import com.cartaxi.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateTimeUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
    }

    public static LocalDateTime parseDateTime(String value, String fieldName) {
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BusinessException(400, fieldName + "格式错误，应为 yyyy-MM-dd HH:mm:ss");
        }
    }
}