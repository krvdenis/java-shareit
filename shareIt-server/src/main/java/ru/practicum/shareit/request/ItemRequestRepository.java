package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.requester.id <> :userId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllOtherItemRequests(@Param("userId") Long userId);

    @Query("SELECT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.offeredItems oi " +
            "LEFT JOIN FETCH oi.owner " +
            "WHERE ir.requester.id = :userId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllByRequesterIdWithOffers(@Param("userId") Long requesterId);

    @Query("SELECT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.offeredItems oi " +
            "LEFT JOIN FETCH oi.owner " +
            "WHERE ir.id = :requestId ")
    Optional<ItemRequest> findAllByIdWithOffers(@Param("requestId") Long requestId);
}
