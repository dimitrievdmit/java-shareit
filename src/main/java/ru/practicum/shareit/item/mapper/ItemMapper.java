package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.mapToBookingInItemDTO;

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
     * Преобразует ItemRequestDTO в доменную модель Item.
     * ID не передаётся — будет получен из репозитория.
     */
    public static Item mapToDomain(ItemBaseRequestDTO itemBaseRequestDTO, Long ownerId) {
        return new Item(
                null,
                ownerId,
                itemBaseRequestDTO.name(),
                itemBaseRequestDTO.description(),
                itemBaseRequestDTO.available(),
                itemBaseRequestDTO.requestId()
        );
    }

    /**
     * Обновляет данные объекта Item, создавая новый экземпляр на основе itemUpdate.
     * Сохраняет неизменным id и ownerId.
     * Обновляет только не‑null поля.
     */
    public static Item updateFromDTO(Item itemUpdate, Item item) {
        return new Item(
                item.getId(),
                item.getOwnerId(),
                (itemUpdate.getName() != null) ? itemUpdate.getName() : item.getName(),
                (itemUpdate.getDescription() != null && !itemUpdate.getDescription().isBlank())
                        ? itemUpdate.getDescription()
                        : item.getDescription(),
                (itemUpdate.getAvailable() != null) ? itemUpdate.getAvailable() : item.getAvailable(),
                (itemUpdate.getRequestId() != null) ? itemUpdate.getRequestId() : item.getRequestId()
        );
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

    public static ItemWithCommentsDTO mapToItemWithCommentsDTO(Item item, List<Comment> comments) {
        // Преобразовываем комментарии в DTO внутри маппера
        List<CommentResponseDTO> commentDtos = CommentMapper.mapToResponseDTOList(comments, item);

        return new ItemWithCommentsDTO(
                item.getId(),
                item.getOwnerId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId(),
                commentDtos
        );
    }
    public static ItemWithBookingDTO mapToItemWithBookingDTO(Item item, List<Booking> bookings, List<CommentResponseDTO> commentDtos) {
        Booking lastBooking = bookings.stream()
                .filter(b -> b.getEnd().isBefore(Instant.now()))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        Booking nextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(Instant.now()))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        return new ItemWithBookingDTO(
                item.getId(),
                item.getOwnerId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId(),
                lastBooking != null ? mapToBookingInItemDTO(lastBooking) : null,
                nextBooking != null ? mapToBookingInItemDTO(nextBooking) : null,
                commentDtos
        );
    }


}
