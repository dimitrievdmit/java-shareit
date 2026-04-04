package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.time.Instant;
import java.util.Collection;

import static ru.practicum.shareit.validator.Validator.*;


@SuppressWarnings({"unused"})
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public Booking create(Booking booking, Long itemId, Long userId) {
        log.info("Создание бронирования вещи {}", itemId);

        // ИД пользователя должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(userId);
        // ИД вещи должен существовать, иначе выбросить 404.
        itemService.throwIfNotExists(itemId);
        // start не может быть равен end, иначе выбросить 400.
        throwIfStartEqualsEnd(booking.getStart(), booking.getEnd());
        //    Если у бронируемой вещи поле available = false, то выбросить ошибку 400.
//        Item fullItem = itemService.get(booking.item().id());
        if (!booking.getItem().getAvailable()) {
            throw new ValidationException("Бронируемая вещь должна быть доступна для бронирования.");
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking updateStatus(Long bookingId, Long userId, BookingStatus bookingStatus) {
        log.info("Обновление статуса бронирования {}", bookingId);
        // Бронирование должно существовать. Иначе, ошибка 404.
        throwIfNotExists(bookingId);

        // Может быть выполнено только владельцем вещи. Иначе, ошибка 403.
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        throwIfNotOwner(bookingId, booking.getItem(), userId);

        // ИД пользователя должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(userId);

        booking.setStatus(bookingStatus);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getById(Long bookingId, Long userId) {
        log.info("Получение бронирования по ИД {}", bookingId);
        // Бронирование должно существовать. Иначе, ошибка 404.
        throwIfNotExists(bookingId);
        // ИД пользователя должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(userId);
        // Может быть выполнено автором бронирования или владельцем вещи. Иначе, ошибка 403.
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        throwIfNotBookerOrOwner(booking, userId);
        return booking;
    }

    @Override
    public Collection<Booking> getByBooker(Long bookerId, BookingState state) {
        log.info("Получение бронирований пользователя {} с фильтром {}", bookerId, state);
        // ИД пользователя должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(bookerId);
        // Бронирования должны возвращаться отсортированными по дате (старта) от более новых к более старым.
        Sort sort = Sort.by("start").descending();
        switch (state) {
            case BookingState.ALL:
                return bookingRepository.findByBookerId(bookerId, sort);
            case BookingState.PAST:
            case BookingState.CURRENT:
            case BookingState.FUTURE:
                Instant date = Instant.now();
                switch (state) {
                    case BookingState.PAST:
                        return bookingRepository.findByBookerIdAndEndIsBefore(bookerId, date, sort);
                    case BookingState.CURRENT:
                        return bookingRepository.findByBookerIdAndActiveOnDate(bookerId, date, sort);
                    case BookingState.FUTURE:
                        return bookingRepository.findByBookerIdAndStartIsAfter(bookerId, date, sort);
                }
            case BookingState.WAITING:
                return bookingRepository.findByBookerIdAndStatusIs(bookerId, BookingStatus.WAITING, sort);
            case BookingState.REJECTED:
                return bookingRepository.findByBookerIdAndStatusIs(bookerId, BookingStatus.REJECTED, sort);
            default:
                throw new InternalServerException(String.format("Не найден маршрут для значения state %s", state));
        }
    }

    @Override
    public Collection<Booking> getByItemOwner(Long ownerId, BookingState state) {
        log.info("Получение бронирований вещей пользователя {} с фильтром {}", ownerId, state);
        // ИД пользователя должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(ownerId);
        // Бронирования должны возвращаться отсортированными по дате (старта) от более новых к более старым.
        Sort sort = Sort.by("start").descending();
        switch (state) {
            case BookingState.ALL:
                return bookingRepository.findByItemOwnerId(ownerId, sort);
            case BookingState.PAST:
            case BookingState.CURRENT:
            case BookingState.FUTURE:
                Instant date = Instant.now();
                switch (state) {
                    case BookingState.PAST:
                        return bookingRepository.findByItemOwnerIdAndEndIsBefore(ownerId, date, sort);
                    case BookingState.CURRENT:
                        return bookingRepository.findByItemOwnerIdAndActiveOnDate(ownerId, date, sort);
                    case BookingState.FUTURE:
                        return bookingRepository.findByItemOwnerIdAndStartIsAfter(ownerId, date, sort);
                }
            case BookingState.WAITING:
                return bookingRepository.findByItemOwnerIdAndStatusIs(ownerId, BookingStatus.WAITING, sort);
            case BookingState.REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatusIs(ownerId, BookingStatus.REJECTED, sort);
            default:
                throw new InternalServerException(String.format("Не найден маршрут для значения state %s", state));
        }
    }

    @Override
    public void throwIfNotExists(Long id) {
        log.info("Проверить, что бронирование существует.");
        if (!bookingRepository.existsById(id)) {
            String errText = "Бронирование с id = " + id + " не найден";
            log.error("Ошибка: {}", errText);
            throw new NotFoundException(errText);
        }
    }


    private void throwIfNotBookerOrOwner(Booking booking, Long userId) {
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwnerId().equals(userId)) {
            String errText = String.format(BOOKING_PERMISSION_ERR_TEXT, userId, booking.getId());
            throwUnauthorised(errText);
        }
    }

    private void throwIfNotOwner(Long bookingId, Item item, Long userId) {
        if (!item.getOwnerId().equals(userId)) {
            String errText = String.format(BOOKING_PERMISSION_ERR_TEXT, userId, bookingId);
            throwUnauthorised(errText);
        }
    }

}
