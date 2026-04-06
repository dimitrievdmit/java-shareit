package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;
import ru.practicum.shareit.item.dto.item.ItemCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemResponseDTO;
import ru.practicum.shareit.item.dto.item.ItemUpdateDTO;
import ru.practicum.shareit.item.dto.item.ItemWithBookingDTO;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    // Long ownerId - владелец вещи. Нужно добавить в Item при его формировании из DTO
    ItemResponseDTO create(ItemCreateDTO itemCreateDTO, Long ownerId);

    // Информацию о вещи может просмотреть любой пользователь.
    ItemWithBookingDTO get(Long id, Long userId);

    // Просмотр владельцем списка всех его вещей
    List<ItemWithBookingDTO> getAllByOwner(Long ownerId);

    // Искать вещи может любой пользователь.
    Collection<ItemResponseDTO> search(String text);

    // Long ownerId - владелец вещи. Проверка: редактировать вещь может только её владелец.
    ItemResponseDTO update(Long itemId, Long ownerId, ItemUpdateDTO itemUpdate);

    // Long ownerId - владелец вещи. Проверка: удалять вещь может только её владелец.
    void delete(Long id, Long ownerId);

    void throwIfNotExists(Long id);

    // Вещь должна существовать. Иначе, ошибка 404.
    // Пользователь должен существовать, иначе выбросить 404.
    // добавить проверку, что пользователь, который пишет комментарий, действительно брал вещь в аренду.
    CommentResponseDTO createComment(Long itemId, Long userId, String text);
}
