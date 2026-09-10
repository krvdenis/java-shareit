package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequestDto;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto bookItem(@RequestHeader(X_SHARER_USER_ID) Long userId,
                             @RequestBody NewBookingRequestDto dto) {
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

    @GetMapping //переделать метод с использованием пагинации?
    public List<BookingDto> getBookingsByBookerId(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                             @RequestParam(defaultValue = "ALL") State state,
                                             @RequestParam(name = "from", defaultValue = "0") Integer from,
                                             @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Поступил запрос на получение информации о забронированных вещах. " +
                "Отправил запрос пользователь с ID: {}.", userId);
        return bookingService.getBookingsByBookerId(userId, state);

    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwnerId(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestParam(defaultValue = "ALL") State state) {
        log.info("Поступил запрос на получение информации о забронированных вещах данного пользователя. " +
                "Отправил запрос пользователь с ID: {}.", userId);
        return bookingService.getBookingsByOwnerId(userId, state);
    }
}