package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.validator.Validator;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDTO
    ) {
        return itemRequestClient.create(userId, itemRequestCreateDTO);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId
    ) {
        return itemRequestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getNonUserRequests(
            @Min(value = 0, message = "Номер страницы должен быть больше или равен 0")
            @RequestParam(defaultValue = "0") Integer from,

            @Positive(message = "Количество запросов на одной странице должно быть больше 0")
            @Max(message = "Количество запросов на одной странице должно быть меньше {value}", value = Validator.MAX_PAGE_SIZE)
            @RequestParam(defaultValue = "10") Integer size,

            @Positive(message = "Ид пользователя должен быть больше 0")
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        return itemRequestClient.getNonUserRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @PathVariable @Positive(message = "Ид запроса должен быть больше 0") Long requestId
    ) {
        return itemRequestClient.getById(userId, requestId);
    }
}