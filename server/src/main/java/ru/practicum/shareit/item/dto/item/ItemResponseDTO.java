package ru.practicum.shareit.item.dto.item;

public record ItemResponseDTO(
        Long id,

        String name,

        String description,

        Boolean available,

        Long requestId
) {
}
