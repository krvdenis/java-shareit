package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.dto.ItemWithBookingDates;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select i from Item i left join fetch i.comments where i.id = :itemId")
    Optional<Item> findByIdWithComments(@Param("itemId") Long itemId);

    @Query(value = "select " +
            "(select max(b.start_time) from bookings b " +
            "   where b.item_id = :itemId " +
            "     and b.end_time < :now " +
            "     and AGE(:now, b.end_time) > INTERVAL '5 minutes') as last_booking, " +
            "(select min(b.start_time) from bookings b " +
            "   where b.item_id = :itemId " +
            "     and b.start_time > :now) as next_booking " +
            "from items i where i.id = :itemId",
            nativeQuery = true)
    Object[] findLastAndNextBookingDates(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    List<Item> findByOwnerId(Long userId);

    @Query(" select i from Item i " +
            "where i.available = true and upper(i.name) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%')) ")
    List<Item> searchByText(@Param("text") String text);

    @Query("select new ru.practicum.shareit.item.dto.ItemWithBookingDates(" +
            "  i.id, " +
            "  i.name, " +
            "  i.description, " +
            "  i.available, " +
            "  (select max(b.startTime) from Booking b where b.item = i and b.startTime < CURRENT_TIMESTAMP), " +
            "  (select min(b.startTime) from Booking b where b.item = i and b.startTime > CURRENT_TIMESTAMP)" +
            ") from Item i where i.id IN :itemIds")
    List<ItemWithBookingDates> findWithBookingDates(@Param("itemIds") List<Long> itemIds);
}