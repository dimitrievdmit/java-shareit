package ru.practicum.shareit.item.dto.item;


import ru.practicum.shareit.booking.dto.BookingInItemDTO;
import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;

import java.util.List;


public record ItemWithBookingDTO(
        Long id,

        Long ownerId,

        String name,

        String description,

        Boolean available,

        Long requestId,

        BookingInItemDTO lastBooking,

        BookingInItemDTO nextBooking,

        List<CommentResponseDTO> comments) {
}
