package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.comments " +
            "WHERE i.id = :itemId")
    Optional<Item> findByIdWithComments(@Param("itemId") Long itemId);

    List<Item> findByOwnerId(Long userId);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = TRUE " +
            "AND UPPER(i.name) LIKE UPPER(CONCAT('%', ?1, '%')) " +
            "OR UPPER(i.description) LIKE UPPER(CONCAT('%', ?1, '%'))")
    List<Item> searchByText(@Param("text") String text);
}