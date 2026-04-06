package ru.practicum.shareit.item.dto.item;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.practicum.shareit.booking.dto.BookingInItemDTO;
import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;

import java.util.List;

import static ru.practicum.shareit.validator.Validator.MAX_DESCRIPTION_LENGTH;


public record ItemWithBookingDTO(
        @Positive(message = "Ид должен быть больше 0") Long id,

        @NotNull(message = "Ид владельца не может быть пустым")

        @Positive(message = "Ид владельца должен быть больше 0") Long ownerId,
        @NotBlank(message = "Название не может быть пустым") String name,

        @NotBlank(message = "Описание не может быть пустым")
        @Size(max = MAX_DESCRIPTION_LENGTH, message = "Описание не может быть длиннее {max} символов") String description,

        @NotNull(message = "Флаг доступности для бронирования не может быть пустым") Boolean available,

        @Positive(message = "Ид запроса должен быть больше 0") Long requestId,

        BookingInItemDTO lastBooking,

        BookingInItemDTO nextBooking,

        List<CommentResponseDTO> comments) {
}
