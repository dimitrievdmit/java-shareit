package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public abstract class BaseItemRepositoryTest {

    protected ItemRepository itemRepository;
    protected final Long ownerId1 = 1L;
    protected final Long ownerId2 = 2L;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
    }

    @Test
    void create_ValidItem_ShouldReturnItemWithGeneratedId() {
        Item item = new Item(null, ownerId1, "Test Item", "Description", true, null);

        Item savedItem = itemRepository.saveAndFlush(item);

        assertNotNull(savedItem.getId());
        assertEquals(ownerId1, savedItem.getOwnerId());
        assertEquals("Test Item", savedItem.getName());
        assertEquals("Description", savedItem.getDescription());
        assertTrue(savedItem.getAvailable());
        assertNull(savedItem.getRequestId());
    }

    @Test
    void create_ItemWithRequestId_ShouldSaveRequestId() {
        Item item = new Item(null, ownerId1, "Request Item", "Desc", true, 42L);

        Item savedItem = itemRepository.saveAndFlush(item);

        assertNotNull(savedItem.getId());
        assertEquals(42L, savedItem.getRequestId());
    }

    @Test
    void get_ExistingItem_ShouldReturnItem() {
        Item item = new Item(null, ownerId1, "Item 1", "Desc 1", true, null);
        Item savedItem = itemRepository.saveAndFlush(item);
        Long itemId = savedItem.getId();

        Item foundItem = itemRepository.findById(itemId).orElse(null);

        assertNotNull(foundItem);
        assertEquals(itemId, foundItem.getId());
        assertEquals("Item 1", foundItem.getName());
    }

    @Test
    void get_NonExistingItem_ShouldReturnNull() {
        assertNull(itemRepository.findById(999L).orElse(null));
    }

    @Test
    void getAllByOwner_ItemsExist_ShouldReturnAllOwnerItems() {
        Item item1 = new Item(null, ownerId1, "Item A", "Desc A", true, null);
        Item item2 = new Item(null, ownerId1, "Item B", "Desc B", false, null);
        Item item3 = new Item(null, ownerId2, "Item C", "Desc C", true, null);

        Item saved1 = itemRepository.saveAndFlush(item1);
        Item saved2 = itemRepository.saveAndFlush(item2);
        itemRepository.saveAndFlush(item3);

        Collection<Item> ownerItems = itemRepository.findAllByOwnerId(ownerId1);

        assertEquals(2, ownerItems.size());
        List<Long> foundIds = ownerItems.stream().map(Item::getId).toList();
        assertTrue(foundIds.contains(saved1.getId()));
        assertTrue(foundIds.contains(saved2.getId()));
    }

    @Test
    void getAllByOwner_NoItems_ShouldReturnEmptyCollection() {
        Collection<Item> items = itemRepository.findAllByOwnerId(999L);
        assertTrue(items.isEmpty());
    }

    @Test
    void search_WithText_ShouldReturnMatchingAvailableItems() {
        Item availableItem1 = new Item(null, ownerId1, "Phone", "Mobile phone", true, null);
        Item availableItem2 = new Item(null, ownerId1, "Laptop", "Gaming laptop", true, null);
        Item unavailableItem = new Item(null, ownerId1, "Tablet", "Old tablet", false, null);

        itemRepository.saveAndFlush(availableItem1);
        itemRepository.saveAndFlush(availableItem2);
        itemRepository.saveAndFlush(unavailableItem);

        Collection<Item> foundItems = itemRepository.findByText("phone");

        assertEquals(1, foundItems.size());
        Item foundItem = foundItems.iterator().next();
        assertEquals("Phone", foundItem.getName());
    }

    @Test
    void search_TextInDescription_ShouldFindItem() {
        Item item = new Item(null, ownerId1, "Camera", "Digital camera with zoom", true, null);
        itemRepository.saveAndFlush(item);

        Collection<Item> foundItems = itemRepository.findByText("zoom");

        assertEquals(1, foundItems.size());
    }

    @Test
    void search_CaseInsensitive_ShouldFindItem() {
        Item item = new Item(null, ownerId1, "Smartphone", "Best SmartPhone ever", true, null);
        itemRepository.saveAndFlush(item);

        Collection<Item> foundItemsLower = itemRepository.findByText("smartphone");
        Collection<Item> foundItemsUpper = itemRepository.findByText("SMARTPHONE");

        assertEquals(1, foundItemsLower.size());
        assertEquals(1, foundItemsUpper.size());
    }

    @Test
    void search_ShouldReturnSortedById() {
        Item item1 = new Item(null, ownerId1, "Alpha", "Description alpha", true, null);
        Item item2 = new Item(null, ownerId1, "Beta", "Description beta", true, null);
        // Сохраняем в обратном порядке, чтобы проверить сортировку
        itemRepository.saveAndFlush(item2);
        itemRepository.saveAndFlush(item1);

        Collection<Item> foundItems = itemRepository.findByText("description");

        List<Long> ids = foundItems.stream().map(Item::getId).toList();
        // Ожидаем сортировку по ID ASC
        assertTrue(ids.get(0) < ids.get(1));
    }

    @Test
    void search_EmptyText_ShouldReturnEmptyCollection() {
        Collection<Item> foundItems = itemRepository.findByText("");
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void search_NullText_ShouldReturnEmptyCollection() {
        Collection<Item> foundItems = itemRepository.findByText(null);
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void update_ExistingItem_ShouldUpdateItem() {
        Item item = new Item(null, ownerId1, "Old Name", "Old Desc", true, null);
        Item savedItem = itemRepository.saveAndFlush(item);
        Long itemId = savedItem.getId();

        Item updatedItem = new Item(itemId, ownerId1, "New Name", "New Desc", false, 10L);
        Item result = itemRepository.saveAndFlush(updatedItem);

        assertEquals(itemId, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertFalse(result.getAvailable());
        assertEquals(10L, result.getRequestId());
    }

    @Test
    void delete_ExistingItem_ShouldRemoveItem() {
        Item item = new Item(null, ownerId1, "Delete Item", "To be deleted", true, null);
        Item savedItem = itemRepository.saveAndFlush(item);
        Long itemId = savedItem.getId();

        itemRepository.deleteById(itemId);
        itemRepository.flush();

        assertNull(itemRepository.findById(itemId).orElse(null));
        assertFalse(itemRepository.existsById(itemId));
    }

    @Test
    void delete_NonExistingItem_ShouldNotThrowException() {
        assertDoesNotThrow(() -> itemRepository.deleteById(999L));
    }

    @Test
    void existsById_ExistingItem_ShouldReturnTrue() {
        Item item = new Item(null, ownerId1, "Check Item", "Desc", true, null);
        Item savedItem = itemRepository.saveAndFlush(item);

        boolean exists = itemRepository.existsById(savedItem.getId());

        assertTrue(exists);
    }

    @Test
    void existsById_NonExistingItem_ShouldReturnFalse() {
        boolean exists = itemRepository.existsById(999L);
        assertFalse(exists);
    }
}