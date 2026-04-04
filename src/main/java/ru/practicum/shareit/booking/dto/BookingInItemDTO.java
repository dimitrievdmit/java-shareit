package ru.practicum.shareit.booking.dto;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record BookingInItemDTO(
        Long id,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant start,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant end) {

}
