package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;

import static ru.practicum.shareit.booking.mapper.BookingMapper.*;
import static ru.practicum.shareit.validator.Validator.*;

@SuppressWarnings({"unused"})
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true) // Все методы по умолчанию только чтение
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    @Transactional
    public BookingResponseDTO create(BookingCreateDTO bookingCreateDTO, Long userId) {
        log.info("Создание бронирования вещи {}", bookingCreateDTO.itemId());

        // ИД пользователя должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(userId);
        // ИД вещи должен существовать, иначе выбросить 404.
        itemService.isExistsOrElseThrow(bookingCreateDTO.itemId());
        // start не может быть равен end, иначе выбросить 400.
        throwIfStartEqualsEnd(bookingCreateDTO.start(), bookingCreateDTO.end());
        // start не может быть позже end
        throwIfStartAfterEnd(bookingCreateDTO.start(), bookingCreateDTO.end());

        Item item = itemRepository.getReferenceById(bookingCreateDTO.itemId());
        // Если у бронируемой вещи поле available = false, то выбросить ошибку 400.
        if (!item.getAvailable()) {
            throw new ValidationException("Бронируемая вещь должна быть доступна для бронирования.");
        }

        User user = userRepository.getReferenceById(userId);
        Booking booking = mapCreateToDomain(bookingCreateDTO, item, user);

        return mapToResponseDTO(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDTO updateStatus(Long bookingId, Long userId, BookingStatus bookingStatus) {
        log.info("Обновление статуса бронирования {}", bookingId);
        // Бронирование должно существовать. Иначе, ошибка 404.
        isExistsOrElseThrow(bookingId);

        // Может быть выполнено только владельцем вещи. Иначе, ошибка 403.
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        throwIfNotOwner(bookingId, booking.getItem(), userId);

        // ИД пользователя должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(userId);

        booking.setStatus(bookingStatus);
        return mapToResponseDTO(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDTO getById(Long bookingId, Long userId) {
        log.info("Получение бронирования по ИД {}", bookingId);
        // Бронирование должно существовать. Иначе, ошибка 404.
        isExistsOrElseThrow(bookingId);
        // ИД пользователя должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(userId);
        // Может быть выполнено автором бронирования или владельцем вещи. Иначе, ошибка 403.
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        throwIfNotBookerOrOwner(booking, userId);
        return mapToResponseDTO(booking);
    }

    @Override
    public Collection<BookingResponseDTO> getByBooker(Long bookerId, BookingState state) {
        log.info("Получение бронирований пользователя {} с фильтром {}", bookerId, state);
        // ИД пользователя должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(bookerId);
        // Бронирования должны возвращаться отсортированными по дате (старта) от более новых к более старым.
        Sort sort = Sort.by("start").descending();
        Collection<Booking> bookings = switch (state) {
            case BookingState.ALL -> bookingRepository.findByBookerId(bookerId, sort);
            case BookingState.PAST ->
                    bookingRepository.findByBookerIdAndEndIsBefore(bookerId, LocalDateTime.now(), sort);
            case BookingState.CURRENT ->
                    bookingRepository.findByBookerIdAndActiveOnDate(bookerId, LocalDateTime.now(), sort);
            case BookingState.FUTURE ->
                    bookingRepository.findByBookerIdAndStartIsAfter(bookerId, LocalDateTime.now(), sort);
            case BookingState.WAITING ->
                    bookingRepository.findByBookerIdAndStatusIs(bookerId, BookingStatus.WAITING, sort);
            case BookingState.REJECTED ->
                    bookingRepository.findByBookerIdAndStatusIs(bookerId, BookingStatus.REJECTED, sort);
            default ->
                    throw new InternalServerException(String.format("Не найден маршрут для значения state %s", state));
        };
        return mapToResponseDTOList(bookings);
    }

    @Override
    public Collection<BookingResponseDTO> getByItemOwner(Long ownerId, BookingState state) {
        log.info("Получение бронирований вещей пользователя {} с фильтром {}", ownerId, state);
        // ИД пользователя должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(ownerId);
        // Бронирования должны возвращаться отсортированными по дате (старта) от более новых к более старым.
        Sort sort = Sort.by("start").descending();
        Collection<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItemOwnerId(ownerId, sort);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndIsBefore(ownerId, LocalDateTime.now(), sort);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndActiveOnDate(ownerId, LocalDateTime.now(), sort);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartIsAfter(ownerId, LocalDateTime.now(), sort);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatusIs(ownerId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatusIs(ownerId, BookingStatus.REJECTED, sort);
            default ->
                    throw new InternalServerException(String.format("Не найден маршрут для значения state %s", state));
        };
        return mapToResponseDTOList(bookings);
    }

    @Override
    public void isExistsOrElseThrow(Long id) {
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