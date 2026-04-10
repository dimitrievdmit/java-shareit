package ru.practicum.shareit.item.dto.item;

public record ItemCreateDTO(

        String name,

        String description,

        Boolean available,

        Long requestId
) implements ItemBaseRequestDTO {
}
