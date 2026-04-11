package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.comment.CommentCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemUpdateDTO;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид владельца должен быть больше 0") Long ownerId,
            @Valid @RequestBody ItemCreateDTO item
    ) {
        return itemClient.create(ownerId, item);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @PathVariable @Positive(message = "Ид вещи должен быть больше 0") Long itemId
    ) {
        return itemClient.get(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид владельца должен быть больше 0") Long ownerId
    ) {
        return itemClient.getAllByOwner(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestParam(name = "text") String text
    ) {
        return itemClient.search(text);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид владельца должен быть больше 0") Long ownerId,
            @PathVariable @Positive(message = "Ид вещи должен быть больше 0") Long itemId,
            @Valid @RequestBody ItemUpdateDTO itemUpdateDTO
    ) {
        return itemClient.update(ownerId, itemId, itemUpdateDTO);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> delete(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид владельца должен быть больше 0") Long ownerId,
            @PathVariable @Positive(message = "Ид вещи должен быть больше 0") Long itemId
    ) {
        return itemClient.delete(ownerId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createComment(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @PathVariable @Positive(message = "Ид вещи должен быть больше 0") Long itemId,
            @Valid @RequestBody CommentCreateDTO commentCreateDTO
    ) {
        return itemClient.createComment(userId, itemId, commentCreateDTO);
    }
}