package ru.practicum.shareit.booking.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

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
        @NotNull(message = "Бронируемая вещь должна быть указана")
        @Positive(message = "Ид бронируемой вещи должен быть больше 0")
        Long itemId,

        @NotNull(message = "Дата и время начала бронирования должна быть указана")
        @FutureOrPresent(message = "Дата и время начала бронирования не может быть в прошлом")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
        Instant start,

        @NotNull(message = "Дата и время конца бронирования должна быть указана")
        @FutureOrPresent(message = "Дата и время конца бронирования не может быть в прошлом")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
        Instant end) {

}
