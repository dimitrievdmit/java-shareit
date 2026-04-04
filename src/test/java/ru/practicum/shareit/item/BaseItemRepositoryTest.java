package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public abstract class BaseItemRepositoryTest {

    protected ItemRepository itemRepository;
    protected final Long ownerId1 = 1L;
    protected final Long ownerId2 = 2L;

    @BeforeEach
    void setUp() {
        // Очистка БД перед каждым тестом
        itemRepository.deleteAll();
    }

    @Test
    void create_ValidItem_ShouldReturnItemWithGeneratedId() {
        // Given
        Item item = new Item(null, ownerId1, "Test Item", "Description", true, null);

        // When
        Item savedItem = itemRepository.saveAndFlush(item);

        // Then
        assertNotNull(savedItem.getId());
        assertEquals(ownerId1, savedItem.getOwnerId());
        assertEquals("Test Item", savedItem.getName());
        assertEquals("Description", savedItem.getDescription());
        assertTrue(savedItem.getAvailable());
    }

    @Test
    void get_ExistingItem_ShouldReturnItem() {
        // Given
        Item item = new Item(null, ownerId1, "Item 1", "Desc 1", true, null);
        Item savedItem = itemRepository.saveAndFlush(item);
        Long itemId = savedItem.getId();

        // When
        Item foundItem = itemRepository.findById(itemId).orElse(null);

        // Then
        assertNotNull(foundItem);
        assertEquals(itemId, foundItem.getId());
        assertEquals("Item 1", foundItem.getName());
    }

    @Test
    void get_NonExistingItem_ShouldReturnNull() {
        // When & Then
        assertNull(itemRepository.findById(999L).orElse(null));
    }

    @Test
    void getAllByOwner_ItemsExist_ShouldReturnSortedItems() {
        // Given: создаём вещи для разных владельцев
        Item item1 = new Item(null, ownerId1, "Item A", "Desc A", true, null);
        Item item2 = new Item(null, ownerId1, "Item B", "Desc B", false, null);
        Item item3 = new Item(null, ownerId2, "Item C", "Desc C", true, null);

        Item saved1 = itemRepository.saveAndFlush(item1);
        Item saved2 = itemRepository.saveAndFlush(item2);
        itemRepository.saveAndFlush(item3);

        // When
        Collection<Item> ownerItems = itemRepository.findAllByOwnerId(ownerId1);

        // Then
        assertEquals(2, ownerItems.size());
        // Проверяем сортировку по ID (saved1 должен быть первым)
        var itemsList = ownerItems.stream().toList();
        assertEquals(saved1.getId(), itemsList.get(0).getId());
        assertEquals(saved2.getId(), itemsList.get(1).getId());
    }

    @Test
    void getAllByOwner_NoItems_ShouldReturnEmptyCollection() {
        // When
        Collection<Item> items = itemRepository.findAllByOwnerId(999L);

        // Then
        assertTrue(items.isEmpty());
    }

    @Test
    void search_WithText_ShouldReturnMatchingAvailableItems() {
        // Given: создаём доступные и недоступные вещи
        Item availableItem1 = new Item(null, ownerId1, "Phone", "Mobile phone", true, null);
        Item availableItem2 = new Item(null, ownerId1, "Laptop", "Gaming laptop", true, null);
        Item unavailableItem = new Item(null, ownerId1, "Tablet", "Old tablet", false, null);

        itemRepository.saveAndFlush(availableItem1);
        itemRepository.saveAndFlush(availableItem2);
        itemRepository.saveAndFlush(unavailableItem);

        // When: ищем по слову "phone"
        Collection<Item> foundItems = itemRepository.findByText("phone");

        // Then: должны найти только доступную вещь с "phone" в названии или описании
        assertEquals(1, foundItems.size());
        var foundItem = foundItems.iterator().next();
        assertEquals("Phone", foundItem.getName());
    }

    @Test
    void search_TextInDescription_ShouldFindItem() {
        // Given
        Item item = new Item(null, ownerId1, "Camera", "Digital camera with zoom", true, null);
        itemRepository.saveAndFlush(item);

        // When
        Collection<Item> foundItems = itemRepository.findByText("zoom");

        // Then
        assertEquals(1, foundItems.size());
    }

    @Test
    void search_EmptyText_ShouldReturnEmptyCollection() {
        // When
        Collection<Item> foundItems = itemRepository.findByText("");

        // Then
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void search_NullText_ShouldReturnEmptyCollection() {
        // When
        Collection<Item> foundItems = itemRepository.findByText(null);

        // Then
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void update_ExistingItem_ShouldUpdateItem() {
        // Given
        Item item = new Item(null, ownerId1, "Old Name", "Old Desc", true, null);
        Item savedItem = itemRepository.saveAndFlush(item);
        Long itemId = savedItem.getId();

        // Создаём обновлённую версию
        Item updatedItem = new Item(itemId, ownerId1, "New Name", "New Desc", false, 10L);

        // When
        Item result = itemRepository.saveAndFlush(updatedItem);

        // Then
        assertEquals(itemId, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertFalse(result.getAvailable());
        assertEquals(10L, result.getRequestId());
    }

    @Test
    void delete_ExistingItem_ShouldRemoveItem() {
        // Given
        Item item = new Item(null, ownerId1, "Delete Item", "To be deleted", true, null);
        Item savedItem = itemRepository.saveAndFlush(item);
        Long itemId = savedItem.getId();

        // When
        itemRepository.deleteById(itemId);

        // Then: вещь не должна находиться после удаления
        assertNull(itemRepository.findById(itemId).orElse(null));
        assertFalse(itemRepository.existsById(itemId));
    }

    @Test
    void delete_NonExistingItem_ShouldNotThrowException() {
        // When & Then: удаление несуществующей вещи не должно вызывать исключений
        assertDoesNotThrow(() -> itemRepository.deleteById(999L));
    }

    @Test
    void checkIfNotExists_ExistingItem_ShouldReturnFalse() {
        // Given
        Item item = new Item(null, ownerId1, "Check Item", "Desc", true, null);
        Item savedItem = itemRepository.saveAndFlush(item);

        // When
        boolean exists = itemRepository.existsById(savedItem.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void checkIfNotExists_NonExistingItem_ShouldReturnTrue() {
        // When
        boolean exists = itemRepository.existsById(999L);

        // Then
        assertFalse(exists);
    }
}
