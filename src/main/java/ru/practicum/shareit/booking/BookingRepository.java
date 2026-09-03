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
            "AND bng.status = APPROVED " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findCurrentBookingsByBookerId(@Param("bookerId") Long bookerId);

    @Query("SELECT bng FROM Booking bng " +
            "WHERE bng.booker.id = :bookerId " +
            "AND bng.endTime < CURRENT_TIMESTAMP " +
            "AND bng.status = APPROVED " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findPastByBookerId(@Param("bookerId") Long bookerId);

    @Query("SELECT bng FROM Booking bng " +
            "WHERE bng.booker.id = :bookerId " +
            "AND bng.startTime > CURRENT_TIMESTAMP " +
            "AND bng.status = APPROVED " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findFutureByBookerId(@Param("bookerId") Long bookerId);

    List<Booking> findByBookerIdAndStatusOrderByStartTimeDesc(Long bookerId, State state);

    List<Booking> findByItemOwnerIdOrderByStartTimeDesc(Long ownerId);

    @Query("SELECT bng FROM Booking bng " +
            "JOIN bng.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND bng.startTime <= CURRENT_TIMESTAMP " +
            "AND bng.endTime >= CURRENT_TIMESTAMP " +
            "AND bng.status = APPROVED " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findCurrentBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT bng FROM Booking bng " +
            "JOIN bng.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND bng.endTime < CURRENT_TIMESTAMP " +
            "AND bng.status = APPROVED " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT bng FROM Booking bng " +
            "JOIN bng.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND bng.startTime > CURRENT_TIMESTAMP " +
            "AND bng.status = APPROVED " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findFutureByOwnerId(@Param("ownerId") Long ownerId);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartTimeDesc(Long ownerId, State state);

    @Query("SELECT NEW ru.practicum.shareit.booking.dto.BookingDates(i.id, b.startTime, b.endTime) " +
            "FROM Booking b " +
            "JOIN b.item AS i " +
            "WHERE i.id IN :itemIds " +
            "AND b.status = :status")
    List<BookingDates> findAllByItemIdsAndStatus(@Param("itemIds") List<Long> itemIds,
                                                 @Param("status") BookingStatus status);

    @Query("SELECT NEW ru.practicum.shareit.booking.dto.BookingDates(it.id, b.startTime, b.endTime) " +
            "FROM Booking b " +
            "JOIN b.item it " +
            "WHERE it.id = :itemId " +
            "AND b.status = :status " +
            "AND b.endTime < :now " +
            "ORDER BY b.endTime DESC")
    Optional<BookingDates> findLastApprovedBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now,
                                                   BookingStatus status);

    @Query("SELECT NEW ru.practicum.shareit.booking.dto.BookingDates(it.id, b.startTime, b.endTime) " +
            "FROM Booking b " +
            "JOIN b.item it " +
            "WHERE it.id = :itemId " +
            "AND b.status = :status " +
            "AND b.startTime > :now " +
            "ORDER BY b.startTime ASC")
    Optional<BookingDates> findNextApprovedBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now,
                                                   BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.endTime > :newStart " +
            "AND b.startTime < :newEnd")
    long countConflicts(@Param("itemId") Long itemId, @Param("newStart") LocalDateTime newStart,
                        @Param("newEnd") LocalDateTime newEnd);

    boolean existsByBookerIdAndItemIdAndEndTimeLessThanAndStatus(Long bookerId, Long itemId, LocalDateTime creationTime,
                                                                 BookingStatus status);
}