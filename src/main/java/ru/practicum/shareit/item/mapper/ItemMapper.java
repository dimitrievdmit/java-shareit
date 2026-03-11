package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemCreateDTO;
import ru.practicum.shareit.item.dto.ItemResponseDTO;
import ru.practicum.shareit.item.dto.ItemUpdateDTO;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

    /**
     * Преобразует доменную модель Item в ItemResponseDTO (с ID).
     */
    public static ItemResponseDTO mapToResponseDTO(Item item) {
        return new ItemResponseDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId()
        );
    }

    /**
     * Преобразует ItemCreateDTO в доменную модель Item для создания.
     * ID устанавливается как null — будет сгенерирован при сохранении.
     */
    public static Item mapToDomainForCreation(ItemCreateDTO itemCreateDTO, Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID не может быть null при создании Item из ItemCreateDTO");
        }

        return new Item(
                null, // ID будет сгенерирован в репозитории
                ownerId,
                itemCreateDTO.name(),
                itemCreateDTO.description(),
                itemCreateDTO.available(),
                itemCreateDTO.requestId()
        );
    }

    /**
     * Обновляет существующий объект Item данными из ItemUpdateDTO.
     * Сохраняет неизменным id и ownerId.
     * Обновляет только не‑null поля.
     */
    public static void updateFromDTO(ItemUpdateDTO itemUpdateDTO, Item item) {
        if (itemUpdateDTO.name() != null) {
            item.setName(itemUpdateDTO.name());
        }
        if (itemUpdateDTO.description() != null && !itemUpdateDTO.description().isBlank()) {
            item.setDescription(itemUpdateDTO.description());
        }
        if (itemUpdateDTO.available() != null) {
            item.setAvailable(itemUpdateDTO.available());
        }
        if (itemUpdateDTO.requestId() != null) {
            item.setRequestId(itemUpdateDTO.requestId());
        }
    }

    /**
     * Преобразует список доменных моделей Item в список ItemResponseDTO с сортировкой по ID.
     */
    public static List<ItemResponseDTO> mapToResponseDTOList(Collection<Item> items) {
        return items.stream()
                .sorted(Comparator.comparing(Item::getId))
                .map(ItemMapper::mapToResponseDTO)
                .collect(Collectors.toList());
    }
}
