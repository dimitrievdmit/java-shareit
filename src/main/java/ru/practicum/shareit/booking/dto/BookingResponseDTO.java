package ru.practicum.shareit.booking.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemResponseDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;

import java.time.Instant;

/**
 * Должен всегда содержать item.id, item.name и booker.id
 */
public record BookingResponseDTO(
        Long id,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant start,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant end,

        ItemResponseDTO item,

        UserResponseDTO booker,

        BookingStatus status) {

}
