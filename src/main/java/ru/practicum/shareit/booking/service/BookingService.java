package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.Collection;

public interface BookingService {

    // ИД пользователя должен существовать, иначе выбросить 404.
    // ИД вещи должен существовать, иначе выбросить 404.
    // start не может быть равен end, иначе выбросить 400.
    // Если у бронируемой вещи поле available = false, то выбросить ошибку 400.
    Booking create(Booking booking, Long itemId, Long userId);

    // Бронирование должно существовать.  Иначе, ошибка 404.
    // ИД пользователя должен существовать, иначе выбросить 404.
    // Может быть выполнено только владельцем вещи. Иначе, ошибка 403.
    Booking updateStatus(Long bookingId, Long userId, BookingStatus bookingStatus);

    // Бронирование должно существовать.  Иначе, ошибка 404.
    // ИД пользователя должен существовать, иначе выбросить 404.
    // Может быть выполнено автором бронирования или владельцем вещи. Иначе, ошибка 403.
    Booking getById(Long bookingId, Long userId);

    // ИД пользователя должен существовать, иначе выбросить 404.
    // Бронирования должны возвращаться отсортированными по дате (старта) от более новых к более старым.
    Collection<Booking> getByBooker(Long requestorId, BookingState state);

    // ИД пользователя должен существовать, иначе выбросить 404.
    // Бронирования должны возвращаться отсортированными по дате (старта) от более новых к более старым.
    Collection<Booking> getByItemOwner(Long ownerId, BookingState state);

    // Проверка, что бронирование существует.
    void throwIfNotExists(Long id);
}
