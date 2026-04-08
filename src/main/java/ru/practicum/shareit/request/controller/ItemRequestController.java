package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.validator.Validator;

import java.util.Collection;

@SuppressWarnings("unused")
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestResponseDto create(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDTO
    ) {
        return itemRequestService.create(itemRequestCreateDTO, userId);
    }

    @GetMapping
    public Collection<ItemRequestResponseDto> getUserRequests(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId
    ) {
        return itemRequestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestResponseDto> getNonUserRequests(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Номер страницы должен быть больше или равен 0")
            Integer from,
            @RequestParam(defaultValue = "10")
            @Positive(message = "Количество запросов на одной странице должно быть больше 0")
            @Max(message = "Количество запросов на одной странице должно быть меньше {value}", value = Validator.MAX_PAGE_SIZE)
            Integer size,
            @RequestHeader("X-Sharer-User-Id")
            @Positive(message = "Ид пользователя должен быть больше 0")
            Long userId
    ) {
        return itemRequestService.getNonUserRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @PathVariable @Positive(message = "Ид бронирования должен быть больше 0") Long requestId
    ) {
        return itemRequestService.getById(requestId, userId);
    }
}
