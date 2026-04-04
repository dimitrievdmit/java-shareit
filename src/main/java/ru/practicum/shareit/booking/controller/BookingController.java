package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

import static ru.practicum.shareit.booking.mapper.BookingMapper.*;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDTO create(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @Valid @RequestBody BookingCreateDTO bookingCreateDTO
    ) {
        Item item = itemService.getReferenceById(bookingCreateDTO.itemId());
        User user = userService.getReferenceById(userId);
        Booking booking = mapCreateToDomain(bookingCreateDTO, item, user);
        return mapToResponseDTO(bookingService.create(booking, bookingCreateDTO.itemId(), userId));
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDTO getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @PathVariable @Positive(message = "Ид бронирования должен быть больше 0") Long bookingId
    ) {
        return mapToResponseDTO(bookingService.getById(bookingId, userId));
    }

    @GetMapping
    public Collection<BookingResponseDTO> getByBooker(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long requestorId,
            @RequestParam(name = "state", defaultValue = BookingState.DEFAULT_VALUE) BookingState state
    ) {
        return mapToResponseDTOList(bookingService.getByBooker(requestorId, state));
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDTO> getByItemOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long ownerId,
            @RequestParam(name = "state", defaultValue = BookingState.DEFAULT_VALUE) BookingState state
    ) {
        return mapToResponseDTOList(bookingService.getByItemOwner(ownerId, state));
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDTO updateStatus(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "Ид пользователя должен быть больше 0") Long userId,
            @PathVariable @Positive(message = "Ид бронирования должен быть больше 0") Long bookingId,
            @RequestParam(name = "approved") Boolean approved
    ) {
        BookingStatus bookingStatus = (approved) ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        return mapToResponseDTO(bookingService.updateStatus(bookingId, userId, bookingStatus));
    }
}
