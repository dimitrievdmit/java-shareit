package ru.yandex.practicum.shareit.item;

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
