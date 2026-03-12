package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseItemRepositoryTest {

    protected ItemRepository itemRepository;
    protected final Long ownerId1 = 1L;
    protected final Long ownerId2 = 2L;

    @BeforeEach
    void setUp() {
        itemRepository = createItemRepository();
    }

    /**
     * Фабричный метод для создания конкретной реализации репозитория.
     * Должен быть переопределён в имплементациях.
     */
    protected abstract ItemRepository createItemRepository();

    @Test
    void create_ValidItem_ShouldReturnItemWithGeneratedId() {
        // Given
        Item item = new Item(null, ownerId1, "Test Item", "Description", true, null);

        // When
        Item savedItem = itemRepository.create(item);

        // Then
        assertNotNull(savedItem.id());
        assertEquals(ownerId1, savedItem.ownerId());
        assertEquals("Test Item", savedItem.name());
        assertEquals("Description", savedItem.description());
        assertTrue(savedItem.available());
    }

    @Test
    void get_ExistingItem_ShouldReturnItem() {
        // Given
        Item item = new Item(null, ownerId1, "Item 1", "Desc 1", true, null);
        Item savedItem = itemRepository.create(item);
        Long itemId = savedItem.id();

        // When
        Item foundItem = itemRepository.get(itemId);

        // Then
        assertNotNull(foundItem);
        assertEquals(itemId, foundItem.id());
        assertEquals("Item 1", foundItem.name());
    }

    @Test
    void get_NonExistingItem_ShouldReturnNull() {
        // When
        Item item = itemRepository.get(999L);

        // Then
        assertNull(item);
    }

    @Test
    void getAllByOwner_ItemsExist_ShouldReturnSortedItems() {
        // Given: создаём вещи для разных владельцев
        Item item1 = new Item(null, ownerId1, "Item A", "Desc A", true, null);
        Item item2 = new Item(null, ownerId1, "Item B", "Desc B", false, null);
        Item item3 = new Item(null, ownerId2, "Item C", "Desc C", true, null);

        Item saved1 = itemRepository.create(item1);
        Item saved2 = itemRepository.create(item2);
        itemRepository.create(item3);

        // When
        Collection<Item> ownerItems = itemRepository.getAllByOwner(ownerId1);

        // Then
        assertEquals(2, ownerItems.size());
        // Проверяем сортировку по ID (saved1 должен быть первым)
        var itemsList = ownerItems.stream().toList();
        assertEquals(saved1.id(), itemsList.get(0).id());
        assertEquals(saved2.id(), itemsList.get(1).id());
    }

    @Test
    void getAllByOwner_NoItems_ShouldReturnEmptyCollection() {
        // When
        Collection<Item> items = itemRepository.getAllByOwner(999L);

        // Then
        assertTrue(items.isEmpty());
    }

    @Test
    void search_WithText_ShouldReturnMatchingAvailableItems() {
        // Given: создаём доступные и недоступные вещи
        Item availableItem1 = new Item(null, ownerId1, "Phone", "Mobile phone", true, null);
        Item availableItem2 = new Item(null, ownerId1, "Laptop", "Gaming laptop", true, null);
        Item unavailableItem = new Item(null, ownerId1, "Tablet", "Old tablet", false, null);

        itemRepository.create(availableItem1);
        itemRepository.create(availableItem2);
        itemRepository.create(unavailableItem);

        // When: ищем по слову "phone"
        Collection<Item> foundItems = itemRepository.search("phone");

        // Then: должны найти только доступную вещь с "phone" в названии или описании
        assertEquals(1, foundItems.size());
        var foundItem = foundItems.iterator().next();
        assertEquals("Phone", foundItem.name());
    }

    @Test
    void search_TextInDescription_ShouldFindItem() {
        // Given
        Item item = new Item(null, ownerId1, "Camera", "Digital camera with zoom", true, null);
        itemRepository.create(item);

        // When
        Collection<Item> foundItems = itemRepository.search("zoom");

        // Then
        assertEquals(1, foundItems.size());
    }

    @Test
    void search_EmptyText_ShouldReturnEmptyCollection() {
        // When
        Collection<Item> foundItems = itemRepository.search("");

        // Then
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void search_NullText_ShouldReturnEmptyCollection() {
        // When
        Collection<Item> foundItems = itemRepository.search(null);

        // Then
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void update_ExistingItem_ShouldUpdateItem() {
        // Given
        Item item = new Item(null, ownerId1, "Old Name", "Old Desc", true, null);
        Item savedItem = itemRepository.create(item);
        Long itemId = savedItem.id();

        // Создаём обновлённую версию
        Item updatedItem = new Item(itemId, ownerId1, "New Name", "New Desc", false, 10L);

        // When
        Item result = itemRepository.update(updatedItem);

        // Then
        assertEquals(itemId, result.id());
        assertEquals("New Name", result.name());
        assertEquals("New Desc", result.description());
        assertFalse(result.available());
        assertEquals(10L, result.requestId());
    }

    @Test
    void delete_ExistingItem_ShouldRemoveItem() {
        // Given
        Item item = new Item(null, ownerId1, "Delete Item", "To be deleted", true, null);
        Item savedItem = itemRepository.create(item);
        Long itemId = savedItem.id();

        // When
        itemRepository.delete(itemId);

        // Then: вещь не должна находиться после удаления
        assertNull(itemRepository.get(itemId));
        assertTrue(itemRepository.checkIfNotExists(itemId));
    }

    @Test
    void delete_NonExistingItem_ShouldNotThrowException() {
        // When & Then: удаление несуществующей вещи не должно вызывать исключений
        assertDoesNotThrow(() -> itemRepository.delete(999L));
    }

    @Test
    void checkIfNotExists_ExistingItem_ShouldReturnFalse() {
        // Given
        Item item = new Item(null, ownerId1, "Check Item", "Desc", true, null);
        Item savedItem = itemRepository.create(item);

        // When
        boolean notExists = itemRepository.checkIfNotExists(savedItem.id());

        // Then
        assertFalse(notExists);
    }

    @Test
    void checkIfNotExists_NonExistingItem_ShouldReturnTrue() {
        // When
        boolean notExists = itemRepository.checkIfNotExists(999L);

        // Then
        assertTrue(notExists);
    }
}
