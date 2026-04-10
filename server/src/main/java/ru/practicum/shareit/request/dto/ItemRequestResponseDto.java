package ru.practicum.shareit.request.dto;


import ru.practicum.shareit.item.dto.item.ItemShortDTO;

import java.time.LocalDateTime;
import java.util.List;

public record ItemRequestResponseDto(
        Long id,
        String description,
        Long requestorId,
        LocalDateTime created,
        List<ItemShortDTO> items
        ) {
}
