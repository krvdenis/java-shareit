package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDtoRequest;

import java.util.List;

public interface BookingService {

    BookingDto createBookingRequest(Long userId, NewBookingDtoRequest dtoRequest);

    BookingDto getBookingById(Long userId, Long bookingId);

    BookingDto updateBookingStatus(Long userId, Long bookingId, Boolean status);

    List<BookingDto> getBookingsByBookerId(Long userId, State state);

    List<BookingDto> getBookingsByOwnerId(Long userId, State state);
}