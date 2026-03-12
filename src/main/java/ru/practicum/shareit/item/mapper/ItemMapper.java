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
                item.id(),
                item.name(),
                item.description(),
                item.available(),
                item.requestId()
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
     * Обновляет данные объекта Item, создавая новый экземпляр record на основе ItemUpdateDTO.
     * Сохраняет неизменным id и ownerId.
     * Обновляет только не‑null поля.
     */
    public static Item updateFromDTO(ItemUpdateDTO itemUpdateDTO, Item item) {
        return new Item(
                item.id(),
                item.ownerId(),
                (itemUpdateDTO.name() != null) ? itemUpdateDTO.name() : item.name(),
                (itemUpdateDTO.description() != null && !itemUpdateDTO.description().isBlank())
                        ? itemUpdateDTO.description()
                        : item.description(),
                (itemUpdateDTO.available() != null) ? itemUpdateDTO.available() : item.available(),
                (itemUpdateDTO.requestId() != null) ? itemUpdateDTO.requestId() : item.requestId()
        );
    }


    /**
     * Преобразует список доменных моделей Item в список ItemResponseDTO с сортировкой по ID.
     */
    public static List<ItemResponseDTO> mapToResponseDTOList(Collection<Item> items) {
        return items.stream()
                .sorted(Comparator.comparing(Item::id))
                .map(ItemMapper::mapToResponseDTO)
                .collect(Collectors.toList());
    }
}
