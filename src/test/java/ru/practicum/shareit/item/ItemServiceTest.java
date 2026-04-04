package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.PermissionException;
import ru.practicum.shareit.item.dto.ItemCreateDTO;
import ru.practicum.shareit.item.dto.ItemResponseDTO;
import ru.practicum.shareit.item.dto.ItemUpdateDTO;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.service.UserService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemServiceImpl itemService;

    private static final Long EXISTING_ITEM_ID = 1L;
    private static final Long NON_EXISTING_ITEM_ID = 999L;
    private static final Long OWNER_ID = 100L;
    private static final Long ANOTHER_OWNER_ID = 200L;

    // --- Тесты для create() ---

    @Test
    void create_ValidData_ShouldCreateItem() {
        // Given
        ItemCreateDTO createDTO = new ItemCreateDTO("New Item", "Description", true, 1L);
        Item expectedItem = new Item(1L, OWNER_ID, "New Item", "Description", true, 1L);
        ItemResponseDTO expectedResponse = ItemMapper.mapToResponseDTO(expectedItem);

        doNothing().when(userService).throwIfNotExists(OWNER_ID);
        when(itemRepository.save(any(Item.class))).thenReturn(expectedItem);

        // When
        ItemResponseDTO result = itemService.create(createDTO, OWNER_ID);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(userService, times(1)).throwIfNotExists(OWNER_ID);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void create_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        ItemCreateDTO createDTO = new ItemCreateDTO("New Item", "Description", true, 1L);

        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).throwIfNotExists(OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.create(createDTO, OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    // --- Тесты для get() ---

    @Test
    void get_ExistingItem_ShouldReturnItem() {
        // Given
        Item existingItem = new Item(EXISTING_ITEM_ID, OWNER_ID, "Item Name", "Description", true, 1L);
        ItemResponseDTO expectedResponse = ItemMapper.mapToResponseDTO(existingItem);

        when(itemRepository.findById(EXISTING_ITEM_ID)).thenReturn(Optional.of(existingItem));
        when(itemRepository.existsById(EXISTING_ITEM_ID)).thenReturn(true);

        // When
        ItemResponseDTO result = itemService.get(EXISTING_ITEM_ID);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(itemRepository).existsById(EXISTING_ITEM_ID);
    }

    @Test
    void get_NonExistingItem_ShouldThrowNotFoundException() {
        // Given
        when(itemRepository.existsById(NON_EXISTING_ITEM_ID)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> itemService.get(NON_EXISTING_ITEM_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    // --- Тесты для getAllByOwner() ---

    @Test
    void getAllByOwner_ExistingOwner_ShouldReturnItems() {
        // Given
        List<Item> ownerItems = Arrays.asList(
                new Item(1L, OWNER_ID, "Item 1", "Desc 1", true, 1L),
                new Item(2L, OWNER_ID, "Item 2", "Desc 2", false, 2L)
        );
        List<ItemResponseDTO> expectedResponses = ItemMapper.mapToResponseDTOList(ownerItems);

        doNothing().when(userService).throwIfNotExists(OWNER_ID);
        when(itemRepository.findAllByOwnerId(OWNER_ID)).thenReturn(ownerItems);

        // When
        Collection<ItemResponseDTO> result = itemService.getAllByOwner(OWNER_ID);

        // Then
        assertThat(result).containsExactlyElementsOf(expectedResponses);
    }

    @Test
    void getAllByOwner_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).throwIfNotExists(OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.getAllByOwner(OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    // --- Тесты для search() ---

    @Test
    void search_WithValidText_ShouldReturnMatchingItems() {
        // Given
        String searchText = "test";
        List<Item> foundItems = List.of(new Item(1L, OWNER_ID, "Test Item", "Test Description", true, 1L));
        List<ItemResponseDTO> expectedResponses = ItemMapper.mapToResponseDTOList(foundItems);

        when(itemRepository.findByText(searchText)).thenReturn(foundItems);

        // When
        Collection<ItemResponseDTO> result = itemService.search(searchText);

        // Then
        assertThat(result).containsExactlyElementsOf(expectedResponses);
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() {
        // Given
        String emptyText = "";

        // When
        Collection<ItemResponseDTO> result = itemService.search(emptyText);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void search_WithNullText_ShouldReturnEmptyList() {

        // When
        Collection<ItemResponseDTO> result = itemService.search(null);

        // Then
        assertThat(result).isEmpty();
    }

    // --- Тесты для update() ---
    @Test
    void update_ExistingItemByOwner_ShouldUpdateItem() {
        // Given
        Long itemId = 1L;
        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Updated Name", "Updated Description", false, 2L);
        Item existingItem = new Item(itemId, OWNER_ID, "Old Name", "Old Description", true, 1L);

        // Создаём ожидаемый результат — новый объект с обновлёнными данными
        Item expectedUpdatedItem = new Item(
                itemId,
                OWNER_ID,
                "Updated Name",
                "Updated Description",
                false,
                2L
        );
        ItemResponseDTO expectedResponse = ItemMapper.mapToResponseDTO(expectedUpdatedItem);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(userService).throwIfNotExists(OWNER_ID);
        // Указываем, что репозиторий должен вернуть ожидаемый обновлённый объект
        when(itemRepository.save(any(Item.class))).thenReturn(expectedUpdatedItem);

        // When
        ItemResponseDTO result = itemService.update(itemId, OWNER_ID, updateDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);

        // Проверяем, что update был вызван ровно один раз с любым объектом Item
        verify(itemRepository, times(1)).save(any(Item.class));

        // Дополнительно можем проверить, что переданный объект соответствует ожиданиям
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        Item capturedItem = itemCaptor.getValue();

        assertThat(capturedItem.getId()).isEqualTo(itemId);
        assertThat(capturedItem.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(capturedItem.getName()).isEqualTo("Updated Name");
        assertThat(capturedItem.getDescription()).isEqualTo("Updated Description");
        assertThat(capturedItem.getAvailable()).isFalse();
        assertThat(capturedItem.getRequestId()).isEqualTo(2L);

        // Убеждаемся, что исходный объект не изменился (важно для record)
        assertThat(existingItem.getName()).isEqualTo("Old Name");
        assertThat(existingItem.getDescription()).isEqualTo("Old Description");
        assertThat(existingItem.getAvailable()).isTrue();
        assertThat(existingItem.getRequestId()).isEqualTo(1L);
    }


    @Test
    void update_PartialUpdateWithOnlyName_ShouldUpdateOnlyName() {
        // Given
        Long itemId = 1L;
        ItemUpdateDTO partialUpdate = new ItemUpdateDTO("New Name", null, null, null);
        Item existingItem = new Item(itemId, OWNER_ID, "Old Name", "Description", true, 1L);

        // Создаём ожидаемый результат — новый объект с обновлённым именем, остальные поля сохранены
        Item expectedUpdatedItem = new Item(
                itemId,
                OWNER_ID,
                "New Name",
                "Description",
                true,
                1L
        );
        ItemResponseDTO expectedResponse = ItemMapper.mapToResponseDTO(expectedUpdatedItem);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(userService).throwIfNotExists(OWNER_ID);
        // Указываем, что репозиторий должен вернуть ожидаемый обновлённый объект
        when(itemRepository.save(any(Item.class))).thenReturn(expectedUpdatedItem);

        // When
        ItemResponseDTO result = itemService.update(itemId, OWNER_ID, partialUpdate);

        // Then
        assertThat(result).isEqualTo(expectedResponse);

        // Проверяем, что update был вызван ровно один раз с любым объектом Item
        verify(itemRepository, times(1)).save(any(Item.class));

        // Используем ArgumentCaptor для проверки переданного в update объекта
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        Item capturedItem = itemCaptor.getValue();

        // Проверяем, что новый объект содержит только обновлённое поле (имя), остальные сохранены
        assertThat(capturedItem.getId()).isEqualTo(itemId);
        assertThat(capturedItem.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(capturedItem.getName()).isEqualTo("New Name");
        assertThat(capturedItem.getDescription()).isEqualTo("Description"); // не изменилось
        assertThat(capturedItem.getAvailable()).isTrue(); // не изменилось
        assertThat(capturedItem.getRequestId()).isEqualTo(1L); // не изменилось

        // Убеждаемся, что исходный объект не изменился (важно для record)
        assertThat(existingItem.getName()).isEqualTo("Old Name");
        assertThat(existingItem.getDescription()).isEqualTo("Description");
        assertThat(existingItem.getAvailable()).isTrue();
        assertThat(existingItem.getRequestId()).isEqualTo(1L);
    }


    @Test
    void update_NonExistingItem_ShouldThrowNotFoundException() {
        // Given
        when(itemRepository.existsById(NON_EXISTING_ITEM_ID)).thenReturn(false);

        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Updated Name", "Updated Description", false, 2L);

        // When & Then
        assertThatThrownBy(() -> itemService.update(NON_EXISTING_ITEM_ID, OWNER_ID, updateDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    @Test
    void update_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        Long itemId = 1L;
        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Updated Name", "Updated Description", false, 2L);

        when(itemRepository.existsById(itemId)).thenReturn(true);
        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).throwIfNotExists(OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.update(itemId, OWNER_ID, updateDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    @Test
    void update_AnotherOwnerTriesToUpdate_ShouldThrowPermissionException() {
        // Given
        Long itemId = 1L;
        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Updated Name", "Updated Description", false, 2L);
        Item existingItem = new Item(itemId, OWNER_ID, "Old Name", "Old Description", true, 1L);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(userService).throwIfNotExists(ANOTHER_OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.update(itemId, ANOTHER_OWNER_ID, updateDTO))
                .isInstanceOf(PermissionException.class)
                .hasMessageContaining("Владелец с ID " + ANOTHER_OWNER_ID + " не имеет прав на выполнение операции с вещью с ID " + itemId);
    }

    // --- Тесты для delete() ---


    @Test
    void delete_ExistingItemByOwner_ShouldDeleteItem() {
        // Given
        Long itemId = 1L;
        Item existingItem = new Item(itemId, OWNER_ID, "Item Name", "Description", true, 1L);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.existsById(EXISTING_ITEM_ID)).thenReturn(true);
        doNothing().when(userService).throwIfNotExists(OWNER_ID);

        // When
        itemService.delete(EXISTING_ITEM_ID, OWNER_ID);

        // Then
        verify(itemRepository, times(1)).deleteById(EXISTING_ITEM_ID);
    }

    @Test
    void delete_NonExistingItem_ShouldThrowNotFoundException() {
        // Given
        when(itemRepository.existsById(NON_EXISTING_ITEM_ID)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> itemService.delete(NON_EXISTING_ITEM_ID, OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    @Test
    void delete_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        when(itemRepository.existsById(EXISTING_ITEM_ID)).thenReturn(true);
        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).throwIfNotExists(OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.delete(EXISTING_ITEM_ID, OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    @Test
    void delete_AnotherOwnerTriesToDelete_ShouldThrowPermissionException() {
        // Given
        Long itemId = 1L;
        Item existingItem = new Item(itemId, OWNER_ID, "Item Name", "Description", true, 1L);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(userService).throwIfNotExists(ANOTHER_OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.delete(itemId, ANOTHER_OWNER_ID))
                .isInstanceOf(PermissionException.class)
                .hasMessageContaining("Владелец с ID " + ANOTHER_OWNER_ID + " не имеет прав на выполнение операции с вещью с ID " + itemId);

        // Then — проверяем, что удаление не было выполнено
        verify(itemRepository, never()).deleteById(itemId);
    }

    // --- Тесты для checkThatItemExists() ---

    @Test
    void throwIfNot_ShouldNotThrowException() {
        // Given
        when(itemRepository.existsById(EXISTING_ITEM_ID)).thenReturn(true);

        // When
        itemService.throwIfNotExists(EXISTING_ITEM_ID);

        // Then — если исключение не выброшено, тест пройден
    }

    @Test
    void throwIfNot_ShouldThrowNotFoundException() {
        // Given
        when(itemRepository.existsById(NON_EXISTING_ITEM_ID)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> itemService.throwIfNotExists(NON_EXISTING_ITEM_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    // --- Дополнительные тесты валидации бизнес‑логики ---
}
