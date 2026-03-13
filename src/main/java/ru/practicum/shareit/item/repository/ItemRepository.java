package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {

    Item create(Item item);

    Item get(Long id);

    Collection<Item> getAllByOwner(Long ownerId);

    Collection<Item> search(String text);

    Item update(Item newUser);

    void delete(Long id);

    boolean checkIfNotExists(Long id);
}
