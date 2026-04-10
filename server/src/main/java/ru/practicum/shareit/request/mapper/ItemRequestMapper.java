package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.item.ItemShortDTO;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemRequestMapper {

    public static ItemRequest mapToDomain(ItemRequestCreateDto dto, Long requestorId, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.description());
        request.setRequestorId(requestorId);
        request.setCreated(created);
        return request;
    }

    public static ItemRequestResponseDto mapToResponseDTO(ItemRequest request, List<ItemShortDTO> items) {
        return new ItemRequestResponseDto(
                request.getId(),
                request.getDescription(),
                request.getRequestorId(),
                request.getCreated(),
                items
        );
    }
}