package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

import static ru.practicum.shareit.item.mapper.ItemMapper.*;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDTO create(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид владельца должен быть больше 0") Long ownerId,
            @Valid @RequestBody ItemCreateDTO item
    ) {
        return mapToResponseDTO(itemService.create(mapToDomain(item, ownerId)));
    }

    @GetMapping("/{itemId}")
    public ItemWithCommentsDTO get(
            @PathVariable @Positive(message = "Ид вещи должен быть больше 0") Long itemId
    ) {
        return itemService.get(itemId);
    }

    @GetMapping
    public Collection<ItemWithBookingDTO> getAllByOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид владельца должен быть больше 0") Long ownerId
    ) {
        return itemService.getAllByOwner(ownerId);
    }

    @GetMapping("/search")
    public Collection<ItemResponseDTO> search(
            @RequestParam(name = "text") String text) {
        return mapToResponseDTOList(itemService.search(text));
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDTO update(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид владельца должен быть больше 0") Long ownerId,
            @PathVariable @Positive(message = "Ид вещи должен быть больше 0") Long itemId,
            @Valid @RequestBody ItemUpdateDTO itemUpdateDTO
    ) {
        return mapToResponseDTO(itemService.update(itemId, mapToDomain(itemUpdateDTO, ownerId)));
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид владельца должен быть больше 0") Long ownerId,
            @PathVariable @Positive(message = "Ид вещи должен быть больше 0") Long itemId
    ) {
        itemService.delete(itemId, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO createComment(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид владельца должен быть больше 0") Long userId,
            @PathVariable @Positive(message = "Ид вещи должен быть больше 0") Long itemId,
            @Valid @RequestBody CommentCreateDTO commentCreateDTO
    ) {
        return CommentMapper.mapToResponseDTO(itemService.createComment(itemId, userId, commentCreateDTO.text()));
    }
}
