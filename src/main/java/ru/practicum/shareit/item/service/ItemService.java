package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemWithBookingDTO;
import ru.practicum.shareit.item.dto.ItemWithCommentsDTO;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    // Long ownerId - владелец вещи. Нужно добавить в Item при его формировании из DTO
    Item create(Item item);

    // Информацию о вещи может просмотреть любой пользователь.
    ItemWithCommentsDTO get(Long id);

    // Просмотр владельцем списка всех его вещей
    List<ItemWithBookingDTO> getAllByOwner(Long ownerId);

    // Искать вещи может любой пользователь.
    Collection<Item> search(String text);

    // Long ownerId - владелец вещи. Проверка: редактировать вещь может только её владелец.
    Item update(Long itemId, Item newItem);

    // Long ownerId - владелец вещи. Проверка: удалять вещь может только её владелец.
    void delete(Long id, Long ownerId);

    void throwIfNotExists(Long id);

    Item getReferenceById(Long itemId);

    // Вещь должна существовать. Иначе, ошибка 404.
    // Пользователь должен существовать, иначе выбросить 404.
    // добавить проверку, что пользователь, который пишет комментарий, действительно брал вещь в аренду.
    Comment createComment(Long itemId, Long userId, String text);
}
