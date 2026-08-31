package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

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
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId); // добавить статус подтвержденные

    @Query("SELECT bng FROM Booking bng " +
            "JOIN bng.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND bng.startTime > CURRENT_TIMESTAMP " +
            "ORDER BY bng.startTime DESC")
    List<Booking> findFutureByOwnerId(@Param("ownerId") Long ownerId);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartTimeDesc(Long ownerId, State state);

    boolean existsByBookerIdAndEndTimeLessThanAndStatus(Long bookerId, LocalDateTime now, BookingStatus status);
}
