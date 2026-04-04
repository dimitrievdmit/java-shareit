package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.user.dto.UserResponseDTO;

import java.time.Instant;

// Используется в JpaRepository только для чтения, так что можно сделать record.

/**
 * @param itemId только ID вещи, без загрузки всей сущности
 */
public record CommentViewDTO(
        Long id,
        String text,
        Long itemId,
        UserResponseDTO author,
        Instant created
) {
    // Явный конструктор для Hibernate
    public CommentViewDTO(Long id, String text, Long itemId, UserResponseDTO author, Instant created) {
        this.id = id;
        this.text = text;
        this.itemId = itemId;
        this.author = author;
        this.created = created;
    }
}
