package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingStatus;

import java.util.Collection;

public interface BookingService {

    // ИД пользователя должен существовать, иначе выбросить 404.
    // ИД вещи должен существовать, иначе выбросить 404.
    // start не может быть равен end, иначе выбросить 400.
    // Если у бронируемой вещи поле available = false, то выбросить ошибку 400.
    BookingResponseDTO create(BookingCreateDTO bookingCreateDTO, Long userId);

    // Бронирование должно существовать.  Иначе, ошибка 404.
    // ИД пользователя должен существовать, иначе выбросить 404.
    // Может быть выполнено только владельцем вещи. Иначе, ошибка 403.
    BookingResponseDTO updateStatus(Long bookingId, Long userId, BookingStatus bookingStatus);

    // Бронирование должно существовать.  Иначе, ошибка 404.
    // ИД пользователя должен существовать, иначе выбросить 404.
    // Может быть выполнено автором бронирования или владельцем вещи. Иначе, ошибка 403.
    BookingResponseDTO getById(Long bookingId, Long userId);

    // ИД пользователя должен существовать, иначе выбросить 404.
    // Бронирования должны возвращаться отсортированными по дате (старта) от более новых к более старым.
    Collection<BookingResponseDTO> getByBooker(Long requestorId, BookingState state);

    // ИД пользователя должен существовать, иначе выбросить 404.
    // Бронирования должны возвращаться отсортированными по дате (старта) от более новых к более старым.
    Collection<BookingResponseDTO> getByItemOwner(Long ownerId, BookingState state);

    // Проверка, что бронирование существует.
    void isExistsOrElseThrow(Long id);
}
