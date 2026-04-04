package ru.practicum.shareit.item.dto;

public interface ItemBaseRequestDTO {

    String name();

    String description();

    Boolean available();

    Long requestId();
}
