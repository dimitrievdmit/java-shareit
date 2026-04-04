package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingInItemDTO;

import java.util.List;

import static ru.practicum.shareit.validator.Validator.MAX_DESCRIPTION_LENGTH;


@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
public class ItemWithBookingDTO {
    private final @Positive(message = "Ид должен быть больше 0") Long id;

    private final @NotNull(message = "Ид владельца не может быть пустым")

    @Positive(message = "Ид владельца должен быть больше 0") Long ownerId;
    private final @NotBlank(message = "Название не может быть пустым") String name;

    private final @NotBlank(message = "Описание не может быть пустым")
    @Size(max = MAX_DESCRIPTION_LENGTH, message = "Описание не может быть длиннее {max} символов") String description;

    private final @NotNull(message = "Флаг доступности для бронирования не может быть пустым") Boolean available;

    private final @Positive(message = "Ид запроса должен быть больше 0") Long requestId;

    private final BookingInItemDTO lastBooking;

    private final BookingInItemDTO nextBooking;

    private final List<CommentResponseDTO> comments;
}
