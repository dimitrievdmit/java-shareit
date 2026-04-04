package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Collection<Item> findAllByOwnerId(Long ownerId);

    @Query("SELECT i FROM Item i WHERE " +
            "(LOWER(i.name) LIKE CONCAT('%', :text, '%') OR " +
            "LOWER(i.description) LIKE CONCAT('%', :text, '%')) " +
            "AND i.available = true " +
            "ORDER BY i.id ASC")
    Collection<Item> findByText(String text);
}
