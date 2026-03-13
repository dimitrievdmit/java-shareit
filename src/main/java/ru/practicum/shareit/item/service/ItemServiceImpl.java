package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.PermissionException;
import ru.practicum.shareit.item.dto.ItemCreateDTO;
import ru.practicum.shareit.item.dto.ItemResponseDTO;
import ru.practicum.shareit.item.dto.ItemUpdateDTO;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@SuppressWarnings({"unused"})
@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    private static final String PERMISSION_ERR_TEXT = "Владелец с ID %d не имеет прав " +
            "на выполнение операции с вещью с ID %d";

    @Override
    public ItemResponseDTO create(ItemCreateDTO itemCreateDTO, Long ownerId) {
        log.info("Создание вещи для владельца с ID {}: {}", ownerId, itemCreateDTO.name());
        userService.checkThatUserExists(ownerId);

        Item item = ItemMapper.mapToDomainForCreation(itemCreateDTO, ownerId);
        Item savedItem = itemRepository.create(item);
        return ItemMapper.mapToResponseDTO(savedItem);
    }

    @Override
    public ItemResponseDTO get(Long id) {
        log.info("Получение вещи по ID {}", id);
        checkThatItemExists(id);

        Item item = itemRepository.get(id);
        return ItemMapper.mapToResponseDTO(item);
    }

    @Override
    public Collection<ItemResponseDTO> getAllByOwner(Long ownerId) {
        log.info("Получение всех вещей для владельца с ID {}", ownerId);
        userService.checkThatUserExists(ownerId);

        Collection<Item> items = itemRepository.getAllByOwner(ownerId);
        return ItemMapper.mapToResponseDTOList(items);
    }

    @Override
    public Collection<ItemResponseDTO> search(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);

        Collection<Item> foundItems = itemRepository.search(text);
        return ItemMapper.mapToResponseDTOList(foundItems);
    }

    @Override
    public ItemResponseDTO update(Long itemId, Long ownerId, ItemUpdateDTO itemUpdateDTO) {
        log.info("Обновление вещи для владельца с ID: {}", ownerId);

        checkThatItemExists(itemId);
        userService.checkThatUserExists(ownerId);
        checkOwnerPermission(itemId, ownerId);

        Item existingItem = itemRepository.get(itemId);
        Item newItem = ItemMapper.updateFromDTO(itemUpdateDTO, existingItem);
        Item updatedItem = itemRepository.update(newItem);
        return ItemMapper.mapToResponseDTO(updatedItem);
    }

    @Override
    public void delete(Long id, Long ownerId) {
        log.info("Удаление вещи с ID {} владельцем с ID {}", id, ownerId);

        checkThatItemExists(id);
        userService.checkThatUserExists(ownerId);
        checkOwnerPermission(id, ownerId);
        itemRepository.delete(id);
    }

    public void checkThatItemExists(Long id) {
        if (itemRepository.checkIfNotExists(id)) {
            String errText = "Вещь с ID = " + id + " не найдена";
            log.error("Ошибка: {}", errText);
            throw new NotFoundException(errText);
        }
    }

    private void checkOwnerPermission(Long itemId, Long ownerId) {
        Item item = itemRepository.get(itemId);
        if (!item.ownerId().equals(ownerId)) {
            String errText = String.format(PERMISSION_ERR_TEXT, ownerId, itemId);
            log.error("Ошибка доступа: {}", errText);
            throw new PermissionException(errText);
        }
    }
}
