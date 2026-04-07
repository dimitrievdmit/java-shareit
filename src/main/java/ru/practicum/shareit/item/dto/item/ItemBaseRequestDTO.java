package ru.practicum.shareit.item.dto.item;

public interface ItemBaseRequestDTO {

    String name();

    String description();

    Boolean available();

    Long requestId();
}
