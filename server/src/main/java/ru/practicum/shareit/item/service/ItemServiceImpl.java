package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;
import ru.practicum.shareit.item.dto.comment.CommentViewDTO;
import ru.practicum.shareit.item.dto.item.ItemCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemResponseDTO;
import ru.practicum.shareit.item.dto.item.ItemUpdateDTO;
import ru.practicum.shareit.item.dto.item.ItemWithBookingDTO;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.item.mapper.ItemMapper.*;
import static ru.practicum.shareit.validator.Validator.*;

@SuppressWarnings({"unused"})
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Создание вещи
     */
    @Override
    @Transactional
    public ItemResponseDTO create(ItemCreateDTO itemCreateDTO, Long ownerId) {
        log.info("Создание вещи для владельца с ID {}: {}", ownerId, itemCreateDTO.name());
        userService.isExistsOrElseThrow(ownerId);
        return mapToResponseDTO(itemRepository.save(mapToDomain(itemCreateDTO, ownerId)));
    }

    /**
     * Получение вещи по ID с комментариями и без бронирований
     */
    @Override
    public ItemWithBookingDTO get(Long id, Long userId) {
        log.info("Получение вещи по ID {} с комментариями и без бронирований", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с ID = " + id + " не найдена"));

        // Загружаем все комментарии для этой вещи без лишних полей
        List<CommentResponseDTO> commentDtos = commentRepository.findByItemIdWithAuthor(id);

        if (isOwner(id, userId)) {
            // Загружаем бронирования
            List<Booking> bookings = bookingRepository.findByItemIdWithItem(id);
            // Создаём DTO с информацией о бронированиях.
            return mapToDTOWithBookings(item, bookings, commentDtos);
        }
        // Создаём DTO без информации о бронированиях.
        return mapToDTOWithoutBookings(item, commentDtos);
    }

    /**
     * Получение всех вещей владельца с бронированиями и комментариями
     */
    @Override
    public List<ItemWithBookingDTO> getAllByOwner(Long ownerId) {
        log.info("Получение всех вещей для владельца с ID {} с комментариями", ownerId);
        userService.isExistsOrElseThrow(ownerId);

        Collection<Item> items = itemRepository.findAllByOwnerId(ownerId);
        log.info("Получен список из {} вещей для владельца с ID {}", items.size(), ownerId);

        if (items.isEmpty()) return Collections.emptyList();

        List<Long> itemIds = items.stream().map(Item::getId).collect(toList());

        // Загружаем бронирования для этих вещей
        List<Booking> bookings = bookingRepository.findByItemIdsWithItem(itemIds);

        // Группируем бронирования по ID вещи
        Map<Long, List<Booking>> bookingsByItemId = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        // Загружаем комментарии с автором и ID вещи за один запрос
        List<CommentViewDTO> commentViewDtos = commentRepository.findByItemIdsWithAuthorNameAndItemId(itemIds);

        // Группируем комментарии по ID вещи — без N+1, используем itemId из проекции
        Map<Long, List<CommentViewDTO>> commentsByItemId = commentViewDtos.stream()
                .collect(Collectors.groupingBy(CommentViewDTO::itemId));

        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), Collections.emptyList());
                    List<CommentViewDTO> itemCommentViews = commentsByItemId.getOrDefault(item.getId(), Collections.emptyList());

                    // Преобразуем через Mapper в DTO
                    List<CommentResponseDTO> itemCommentDtos = CommentMapper.mapViewToResponseDTOList(itemCommentViews);

                    return mapToDTOWithBookings(item, itemBookings, itemCommentDtos);
                })
                .toList();
    }

    /**
     * Поиск вещей по тексту
     */
    @Override
    public Collection<ItemResponseDTO> search(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);

        if (text == null || text.trim().isEmpty()) return Collections.emptyList();

        String searchText = text.trim();
        Collection<Item> foundItems = itemRepository.findByText(searchText);
        log.info("По запросу '{}' найдено {} доступных вещей", searchText, foundItems.size());
        return mapToResponseDTOList(foundItems);
    }

    /**
     * Обновление вещи
     */
    @Override
    @Transactional
    public ItemResponseDTO update(Long itemId, Long ownerId, ItemUpdateDTO itemUpdate) {
        log.info("Обновление вещи для владельца с ID: {}", ownerId);

        isExistsOrElseThrow(itemId);
        userService.isExistsOrElseThrow(ownerId);
        throwIfNotOwner(itemId, ownerId);

        Item existingItem = itemRepository.findById(itemId).orElseThrow();
        Item newItem = ItemMapper.updateFromDTO(itemUpdate, existingItem);
        return mapToResponseDTO(itemRepository.save(newItem));
    }

    /**
     * Удаление вещи
     */
    @Override
    @Transactional
    public void delete(Long id, Long ownerId) {
        log.info("Удаление вещи с ID {} владельцем с ID {}", id, ownerId);

        isExistsOrElseThrow(id);
        userService.isExistsOrElseThrow(ownerId);
        throwIfNotOwner(id, ownerId);
        itemRepository.deleteById(id);
    }

    /**
     * Создание комментария к вещи
     */
    @Override
    @Transactional
    public CommentResponseDTO createComment(Long itemId, Long userId, String text) {
        isExistsOrElseThrow(itemId);
        userService.isExistsOrElseThrow(userId);
        throwIfNotPastBooker(itemId, userId);

        Item item = itemRepository.getReferenceById(itemId);
        User user = userRepository.getReferenceById(userId);

        Comment comment = CommentMapper.mapCreateToDomain(text, item, user);
        return CommentMapper.mapToResponseDTO(commentRepository.save(comment));
    }

    /**
     * Проверяет, что вещь существует
     */
    @Override
    public void isExistsOrElseThrow(Long id) {
        if (!itemRepository.existsById(id)) {
            String errText = "Вещь с ID = " + id + " не найдена";
            log.error("Ошибка: {}", errText);
            throw new NotFoundException(errText);
        }
    }

    private void throwIfNotPastBooker(Long itemId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> pastBookings = bookingRepository.findPastApprovedBookingsByItemIdAndBookerId(itemId, userId, now);

        if (pastBookings.isEmpty()) {
            throwValidation(COMMENT_CREATE_VALIDATION_ERR_TEXT);
        }
    }

    /**
     * Проверяет, что пользователь является владельцем вещи
     */
    private void throwIfNotOwner(Long itemId, Long ownerId) {
        if (!isOwner(itemId, ownerId)) {
            String errText = String.format(ITEM_PERMISSION_ERR_TEXT, ownerId, itemId);
            throwUnauthorised(errText);
        }
    }

    /**
     * Проверяет, что пользователь является владельцем вещи
     */
    private boolean isOwner(Long itemId, Long ownerId) {
        Item item = itemRepository.findById(itemId).orElseThrow();
        return item.getOwnerId().equals(ownerId);
    }
}