package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDtoRequest;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(X_SHARER_USER_ID) Long userId,
                             @RequestBody @Valid NewBookingDtoRequest dto) {
        log.info("Поступил запрос на создание бронирования: {}. Отправил запрос пользователь с ID: {}.", dto, userId);
        return bookingService.createBookingRequest(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBookingStatus(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                          @RequestParam("approved") Boolean status,
                                          @PathVariable Long bookingId) {
        log.info("Поступил запрос на изменение статуса бронирования: {} на {}. Отправил запрос пользователь с ID: {}.",
                bookingId, status, userId);
        return bookingService.updateBookingStatus(userId, bookingId, status);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(X_SHARER_USER_ID) Long userId, @PathVariable Long bookingId) {
        log.info("Поступил запрос на получение информации о бронировании с ID: {}. " +
                "Отправил запрос пользователь с ID: {}.", bookingId, userId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllByBookerId(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") State state) {
        log.info("Поступил запрос на получение информации о забронированных вещах. " +
                "Отправил запрос пользователь с ID: {}.", userId);
        return bookingService.getBookingsByBookerId(userId, state);

    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwnerId(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") State state) {
        log.info("Поступил запрос на получение информации о забронированных вещах данного пользователя. " +
                "Отправил запрос пользователь с ID: {}.", userId);
        return bookingService.getBookingsByOwnerId(userId, state);
    }
}