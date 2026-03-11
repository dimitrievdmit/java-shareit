package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDTO;
import ru.practicum.shareit.item.dto.ItemResponseDTO;
import ru.practicum.shareit.item.dto.ItemUpdateDTO;

import java.util.Collection;

public interface ItemService {
    // Long ownerId - владелец вещи. Нужно добавить в Item при его формировании из DTO
    ItemResponseDTO create(ItemCreateDTO item, Long ownerId);

    // Информацию о вещи может просмотреть любой пользователь.
    ItemResponseDTO get(Long id);

    // Просмотр владельцем списка всех его вещей
    Collection<ItemResponseDTO> getAllByOwner(Long ownerId);

    // Искать вещи может любой пользователь.
    Collection<ItemResponseDTO> search(String text);

    // Long ownerId - владелец вещи. Проверка: редактировать вещь может только её владелец.
    ItemResponseDTO update(Long itemId, Long ownerId, ItemUpdateDTO newItem);

    // Long ownerId - владелец вещи. Проверка: удалять вещь может только её владелец.
    void delete(Long id, Long ownerId);

    void checkThatItemExists(Long id);
}
