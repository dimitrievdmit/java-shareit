package ru.practicum.shareit.item.dto.comment;

import java.time.LocalDateTime;

public record CommentResponseDTO(
        Long id,
        String text,
        String authorName,

        LocalDateTime created
) {
}
