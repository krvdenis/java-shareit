package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select i from Item i left join fetch i.comments where i.id = :itemId")
    Optional<Item> findByIdWithComments(@Param("itemId") Long itemId);

    List<Item> findByOwnerId(Long userId);

    @Query(" select i from Item i " +
            "where i.available = true and upper(i.name) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%')) ")
    List<Item> searchByText(@Param("text") String text);
}