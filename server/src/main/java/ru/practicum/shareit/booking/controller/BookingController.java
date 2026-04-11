package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDTO create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody BookingCreateDTO bookingCreateDTO
    ) {
        return bookingService.create(bookingCreateDTO, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDTO getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId
    ) {
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public Collection<BookingResponseDTO> getByBooker(
            @RequestHeader("X-Sharer-User-Id") Long requestorId,
            @RequestParam(name = "state", defaultValue = BookingState.DEFAULT_VALUE) BookingState state
    ) {
        return bookingService.getByBooker(requestorId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDTO> getByItemOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(name = "state", defaultValue = BookingState.DEFAULT_VALUE) BookingState state
    ) {
        return bookingService.getByItemOwner(ownerId, state);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDTO updateStatus(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam(name = "approved") Boolean approved
    ) {
        BookingStatus bookingStatus = (approved) ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        return bookingService.updateStatus(bookingId, userId, bookingStatus);
    }
}
