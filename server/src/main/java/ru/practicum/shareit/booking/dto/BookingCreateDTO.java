package ru.practicum.shareit.booking.dto;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Валидация при создании:
 * ИД пользователя должен существовать, иначе выбросить 400/404 - проверка в сервисе
 * ИД вещи должен существовать, иначе выбросить 400/404 - проверка в сервисе
 * start не может быть равен end, иначе выбросить 400 - проверка в сервисе
 * end не может быть в прошлом, иначе выбросить 400 - валидация в CreateDTO
 * start не может быть null, иначе выбросить 400 - валидация в CreateDTO
 * end не может быть null, иначе выбросить 400 - валидация в CreateDTO
 * start не может быть в прошлом, иначе выбросить 400 - валидация в CreateDTO
 */
public record BookingCreateDTO(
        Long itemId,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
        LocalDateTime start,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
        LocalDateTime end) {
}
