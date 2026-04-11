package ru.practicum.shareit.item.dto.item;

public record ItemUpdateDTO(

        String name,

        String description,

        Boolean available,

        Long requestId
) implements ItemBaseRequestDTO {
}
