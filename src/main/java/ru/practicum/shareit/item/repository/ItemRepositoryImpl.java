package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

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
                item.ownerId(),
                item.name(),
                item.description(),
                item.available(),
                item.requestId()
        );
        items.put(newId, newItem);
        log.info("Создана вещь с ID {}: {}", newId, newItem.name());
        return newItem;
    }

    @Override
    public Item get(Long id) {
        return items.get(id);
    }

    @Override
    public Collection<Item> getAllByOwner(Long ownerId) {
        List<Item> ownerItems = items.values().stream()
                .filter(item -> item.ownerId().equals(ownerId))
                .sorted(Comparator.comparing(Item::id)) // сортировка по ID
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
                .filter(Item::available) // только доступные для аренды
                .filter(item ->
                        item.name().toLowerCase().contains(searchText) ||
                                item.description().toLowerCase().contains(searchText)
                )
                .sorted(Comparator.comparing(Item::id)) // сортировка по ID
                .collect(Collectors.toList());

        log.info("По запросу '{}' найдено {} доступных вещей", searchText, foundItems.size());
        return foundItems;
    }

    @Override
    public Item update(Item newItem) {
        Long itemId = newItem.id();
        items.put(itemId, newItem);
        log.info("Обновлена вещь с ID {}: {}", itemId, newItem.name());
        return newItem;
    }

    @Override
    public void delete(Long id) {
        if (items.remove(id) != null) {
            log.info("Удалена вещь с ID {}", id);
        } else {
            log.warn("Попытка удаления несуществующей вещи с ID {}", id);
        }
    }

    @Override
    public boolean checkIfNotExists(Long id) {
        boolean notExists = !items.containsKey(id);
        if (notExists) {
            log.warn("Вещь с ID {} не найдена в хранилище", id);
        }
        return notExists;
    }
}
