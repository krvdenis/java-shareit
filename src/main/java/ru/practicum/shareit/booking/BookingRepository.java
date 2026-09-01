package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.dto.BookingDates;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartTimeDesc(Long bookerId);

    @Query("SELECT bng FROM Booking bng " +
            "WHERE bng.booker.id = :bookerId " +
            "AND bng.startTime <= CURRENT_TIMESTAMP " +
            "AND bng.endTime >= CURRENT_TIMESTAMP " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findCurrentBookingsByBookerId(@Param("bookerId") Long bookerId);

    @Query("SELECT bng FROM Booking bng " +
            "WHERE bng.booker.id = :bookerId " +
            "AND bng.endTime < CURRENT_TIMESTAMP " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findPastByBookerId(@Param("bookerId") Long bookerId);

    @Query("SELECT bng FROM Booking bng " +
            "WHERE bng.booker.id = :bookerId " +
            "AND bng.startTime > CURRENT_TIMESTAMP " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findFutureByBookerId(@Param("bookerId") Long bookerId);

    List<Booking> findByBookerIdAndStatusOrderByStartTimeDesc(Long bookerId, State state);

    List<Booking> findByItemOwnerIdOrderByStartTimeDesc(Long ownerId);

    @Query("SELECT bng FROM Booking bng " +
            "JOIN bng.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND bng.startTime <= CURRENT_TIMESTAMP " +
            "AND bng.endTime >= CURRENT_TIMESTAMP " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findCurrentBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT bng FROM Booking bng " +
            "JOIN bng.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND bng.endTime < CURRENT_TIMESTAMP " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT bng FROM Booking bng " +
            "JOIN bng.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND bng.startTime > CURRENT_TIMESTAMP " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findFutureByOwnerId(@Param("ownerId") Long ownerId);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartTimeDesc(Long ownerId, State state);

    @Query("select new ru.practicum.shareit.booking.dto.BookingDates(it.id, b.startTime, b.endTime) " +
            "from Booking b " +
            "join b.item as it " +
            "where it.id = :itemId " +
            "and b.endTime < :now " +
            "order by b.endTime desc")
    Optional<BookingDates> findLastBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query(value = "SELECT * FROM (" +
            "   SELECT " +
            "       it.id AS item_id, " +
            "       b.start_time AS start, " +
            "       b.end_time AS end, " +
            "       ROW_NUMBER() OVER (PARTITION BY it.id ORDER BY b.end_time DESC) AS rn " +
            "   FROM bookings b " +
            "   JOIN items it ON b.item_id = it.id " +
            "   WHERE it.id IN (:itemIds) " +
            "     AND b.end_time < :now " +
            ") sub " +
            "WHERE sub.rn = 1",
            nativeQuery = true)
    List<BookingDates> findLastBookings(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.dto.BookingDates(it.id, b.startTime, b.endTime) " +
            "from Booking b " +
            "join b.item as it " +
            "where it.id = :itemId " +
            "and b.startTime > :now " +
            "order by b.startTime asc")
    Optional<BookingDates> findNextBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query(value = "SELECT * FROM (" +
            "   SELECT " +
            "       it.id AS item_id, " +
            "       b.start_time AS start, " +
            "       b.end_time AS end, " +
            "       ROW_NUMBER() OVER (PARTITION BY it.id ORDER BY b.start_time ASC) AS rn " +
            "   FROM bookings b " +
            "   JOIN items it ON b.item_id = it.id " +
            "   WHERE it.id IN (:itemIds) " +
            "     AND b.start_time > :now " +
            ") sub " +
            "WHERE sub.rn = 1",
            nativeQuery = true)
    List<BookingDates> findNextBookings(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.endTime > :newStart " +
            "AND b.startTime < :newEnd")
    long countConflicts(@Param("itemId") Long itemId, @Param("newStart") LocalDateTime newStart,
                        @Param("newEnd") LocalDateTime newEnd);

    boolean existsByBookerIdAndEndTimeLessThanAndStatus(Long bookerId, LocalDateTime now, BookingStatus status);
}