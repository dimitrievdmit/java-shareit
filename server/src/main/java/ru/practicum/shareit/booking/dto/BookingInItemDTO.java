package ru.practicum.shareit.booking.dto;


import java.time.LocalDateTime;

public record BookingInItemDTO(
        Long id,


        LocalDateTime start,


        LocalDateTime end) {

}
