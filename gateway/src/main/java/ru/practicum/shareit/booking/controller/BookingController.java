package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingState;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @Valid @RequestBody BookingCreateDTO bookingCreateDTO
    ) {
        return bookingClient.create(userId, bookingCreateDTO);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @PathVariable @Positive(message = "Ид бронирования должен быть больше 0") Long bookingId
    ) {
        return bookingClient.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getByBooker(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long requestorId,
            @RequestParam(name = "state", defaultValue = BookingState.DEFAULT_VALUE) BookingState state
    ) {
        return bookingClient.getByBooker(requestorId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getByItemOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long ownerId,
            @RequestParam(name = "state", defaultValue = BookingState.DEFAULT_VALUE) BookingState state
    ) {
        return bookingClient.getByItemOwner(ownerId, state);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateStatus(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @PathVariable @Positive(message = "Ид бронирования должен быть больше 0") Long bookingId,
            @RequestParam(name = "approved") Boolean approved
    ) {
        return bookingClient.updateStatus(userId, bookingId, approved);
    }
}
