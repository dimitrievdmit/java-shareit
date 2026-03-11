package ru.yandex.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
@Slf4j
@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1L;

    @Override
    public Item create(Item item) {
        Long newId = nextId++;
        Item newItem = new Item(
                newId,
                item.getOwnerId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId()
        );
        items.put(newId, newItem);
        log.info("Создана вещь с ID {}: {}", newId, newItem.getName());
        return newItem;
    }

    @Override
    public Item get(Long id) {
        return items.get(id);
    }

    @Override
    public Collection<Item> getAllByOwner(Long ownerId) {
        List<Item> ownerItems = items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .sorted(Comparator.comparing(Item::getId)) // сортировка по ID
                .collect(Collectors.toList());

        log.info("Получен список из {} вещей для владельца с ID {}", ownerItems.size(), ownerId);
        return ownerItems;
    }

    @Override
    public Collection<Item> search(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.debug("Пустой или null текст для поиска — возвращаем пустой список");
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase().trim();

        List<Item> foundItems = items.values().stream()
                .filter(Item::getAvailable) // только доступные для аренды
                .filter(item ->
                        item.getName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText)
                )
                .sorted(Comparator.comparing(Item::getId)) // сортировка по ID
                .collect(Collectors.toList());

        log.info("По запросу '{}' найдено {} доступных вещей", searchText, foundItems.size());
        return foundItems;
    }

    @Override
    public Item update(Item newItem) {
        Long itemId = newItem.getId();
        items.put(itemId, newItem);
        log.info("Обновлена вещь с ID {}: {}", itemId, newItem.getName());
        return newItem;
    }

    @Override
    public void delete(Long id) {
        if (items.remove(id) != null) {
            log.info("Удалена вещь с ID {}", id);
        } else {
            log.debug("Попытка удаления несуществующей вещи с ID {}", id);
        }
    }

    @Override
    public boolean checkIfNotExists(Long id) {
        boolean notExists = !items.containsKey(id);
        if (notExists) {
            log.debug("Вещь с ID {} не найдена в хранилище", id);
        }
        return notExists;
    }
}
