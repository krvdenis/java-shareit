package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDtoRequest;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@Slf4j
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;


    @PostMapping
    public BookingDto create(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestBody @Valid NewBookingDtoRequest dto) {
        log.info("Поступил запрос на создание бронирования: {}. Отправил запрос пользователь с ID: {}.", dto, userId);
        return bookingService.createBookingRequest(userId, dto);
    }

    ;

    @PatchMapping("/{bookingId}")
    public BookingDto updateBookingStatus(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestParam("approved") Boolean status,
            @PathVariable Long bookingId) {
        if (status == true) {
            return bookingService.approveBookingStatus(userId, bookingId);
        } else {
            return bookingService.rejectBookingStatus(userId, bookingId);
        }
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(X_SHARER_USER_ID) Long userId, @PathVariable Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllByBookerId(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.getBookingsByBookerId(userId, state);

    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwnerId(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.getBookingsByOwnerId(userId, state);
    }

}