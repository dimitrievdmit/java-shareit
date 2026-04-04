package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentViewDTO;
import ru.practicum.shareit.item.dto.CommentResponseDTO;
import ru.practicum.shareit.item.dto.ItemWithBookingDTO;
import ru.practicum.shareit.item.dto.ItemWithCommentsDTO;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.shareit.validator.Validator.*;

@SuppressWarnings({"unused"})
@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    private final static int PAGE_SIZE = 20;

    @Override
    public Item create(Item item) {
        log.info("Создание вещи для владельца с ID {}: {}", item.getOwnerId(), item.getName());
        userService.throwIfNotExists(item.getOwnerId());
        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemWithCommentsDTO get(Long id) {
        log.info("Получение вещи по ID {} с комментариями (без бронирований)", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с ID = " + id + " не найдена"));

        // Загружаем все комментарии для этой вещи вместе с авторами за один запрос
        List<Comment> comments = commentRepository.findByItemIdWithAuthor(id);

        // Создаём DTO только с комментариями (без информации о бронированиях).
        // Маппер использует заранее загруженную вещь вместо comment.getItem()
        return ItemMapper.mapToItemWithCommentsDTO(item, comments);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemWithBookingDTO> getAllByOwner(Long ownerId) {
        log.info("Получение всех вещей для владельца с ID {} с комментариями", ownerId);
        userService.throwIfNotExists(ownerId);

        Collection<Item> items = itemRepository.findAllByOwnerId(ownerId);
        log.info("Получен список из {} вещей для владельца с ID {}", items.size(), ownerId);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // Извлекаем ID вещей
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // Загружаем все бронирования для этих вещей
        List<Booking> bookings = bookingRepository.findByItemIdsWithItem(itemIds);

        // Группируем бронирования по ID вещи
        Map<Long, List<Booking>> bookingsByItemId = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        // Загружаем комментарии с автором и ID вещи за один запрос
        List<CommentViewDTO> commentViewDtos = commentRepository.findByItemIdsWithAuthorAndItemId(itemIds);

        // Группируем комментарии по ID вещи — без N+1, используем itemId из проекции
        Map<Long, List<CommentViewDTO>> commentsByItemId = commentViewDtos.stream()
                .collect(Collectors.groupingBy(CommentViewDTO::itemId));

        // Собираем итоговый результат
        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), Collections.emptyList());
                    List<CommentViewDTO> itemCommentViewDtos = commentsByItemId.getOrDefault(item.getId(), Collections.emptyList());

                    // Преобразовываем проекции в CommentResponseDTO через маппер
                    List<CommentResponseDTO> itemCommentDtos = CommentMapper.mapViewToResponseDTOList(itemCommentViewDtos, item);

                    // Передаём готовые DTO в маппер — преобразование уже выполнено
                    return ItemMapper.mapToItemWithBookingDTO(item, itemBookings, itemCommentDtos);
                })
                .collect(Collectors.toList());
    }



    @Override
    public Collection<Item> search(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);

        if (text == null || text.trim().isEmpty()) {
            log.debug("Пустой или null текст для поиска — возвращаем пустой список");
            return Collections.emptyList();
        }
        String searchText = text.trim().toLowerCase();
        Collection<Item> foundItems = itemRepository.findByText(searchText);
        log.info("По запросу '{}' найдено {} доступных вещей", searchText, foundItems.size());
        return foundItems;
    }

    @Override
    public Item update(Long itemId, Item itemUpdate) {
        log.info("Обновление вещи для владельца с ID: {}", itemUpdate.getOwnerId());

        throwIfNotExists(itemId);
        userService.throwIfNotExists(itemUpdate.getOwnerId());
        throwIfNotOwner(itemId, itemUpdate.getOwnerId());

        Item existingItem = itemRepository.findById(itemId).orElseThrow();
        Item newItem = ItemMapper.updateFromDTO(itemUpdate, existingItem);
        return itemRepository.save(newItem);
    }

    @Override
    public void delete(Long id, Long ownerId) {
        log.info("Удаление вещи с ID {} владельцем с ID {}", id, ownerId);

        throwIfNotExists(id);
        userService.throwIfNotExists(ownerId);
        throwIfNotOwner(id, ownerId);
        itemRepository.deleteById(id);
    }

    public void throwIfNotExists(Long id) {
        if (!itemRepository.existsById(id)) {
            String errText = "Вещь с ID = " + id + " не найдена";
            log.error("Ошибка: {}", errText);
            throw new NotFoundException(errText);
        }
    }

    @Override
    public Item getReferenceById(Long itemId) {
        return itemRepository.getReferenceById(itemId);
    }

    @Override
    public Comment createComment(Long itemId, Long userId, String text) {
        // Вещь должна существовать. Иначе, ошибка 404.
        throwIfNotExists(itemId);
        // Пользователь должен существовать, иначе выбросить 404.
        userService.throwIfNotExists(userId);
        // добавить проверку, что пользователь, который пишет комментарий, действительно брал вещь в аренду.
        throwIfNotPastBooker(itemId, userId);

        Item item = itemRepository.getReferenceById(itemId);
        User user = userService.getReferenceById(userId);
        return commentRepository.save(CommentMapper.mapCreateToDomain(text, item, user));
    }

    private void throwIfNotOwner(Long itemId, Long ownerId) {
        Item item = itemRepository.findById(itemId).orElseThrow();
        if (!item.getOwnerId().equals(ownerId)) {
            String errText = String.format(ITEM_PERMISSION_ERR_TEXT, ownerId, itemId);
            throwUnauthorised(errText);
        }
    }

    private void throwIfNotPastBooker(Long itemId, Long userId) {
        Instant now = Instant.now();
        List<Booking> pastBookings = bookingRepository.findPastBookingsByItemIdAndBookerId(itemId, userId, now);

        if (pastBookings.isEmpty()) {
            throwValidation(COMMENT_CREATE_VALIDATION_ERR_TEXT);
        }
    }

}
