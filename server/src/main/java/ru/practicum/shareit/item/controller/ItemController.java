package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.comment.CommentCreateDTO;
import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;
import ru.practicum.shareit.item.dto.item.ItemCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemResponseDTO;
import ru.practicum.shareit.item.dto.item.ItemUpdateDTO;
import ru.practicum.shareit.item.dto.item.ItemWithBookingDTO;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDTO create(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestBody ItemCreateDTO item
    ) {
        return itemService.create(item, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingDTO get(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId
    ) {
        return itemService.get(itemId, userId);
    }

    @GetMapping
    public Collection<ItemWithBookingDTO> getAllByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId
    ) {
        return itemService.getAllByOwner(ownerId);
    }

    @GetMapping("/search")
    public Collection<ItemResponseDTO> search(
            @RequestParam(name = "text") String text) {
        return itemService.search(text);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDTO update(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDTO itemUpdateDTO
    ) {
        return itemService.update(itemId, ownerId, itemUpdateDTO);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PathVariable Long itemId
    ) {
        itemService.delete(itemId, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO createComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentCreateDTO commentCreateDTO
    ) {
        return itemService.createComment(itemId, userId, commentCreateDTO.text());
    }
}
