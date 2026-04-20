package ru.practicum.shareit.item.dto.comment;

import java.time.LocalDateTime;

// Используется в JpaRepository только для чтения, так что можно сделать record.

/**
 * @param itemId только ID вещи, без загрузки всей сущности
 */
public record CommentViewDTO(
        Long id,
        String text,
        Long itemId,
        String authorName,
        LocalDateTime created
) {
}
