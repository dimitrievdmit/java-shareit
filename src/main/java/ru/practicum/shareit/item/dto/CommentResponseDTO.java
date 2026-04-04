package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.shareit.user.dto.UserResponseDTO;

import java.time.Instant;


public record CommentResponseDTO(
        Long id,
        String text,
        ItemResponseDTO item,
        UserResponseDTO author,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant created
) {
}
