package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequestDto;

import java.util.List;

public interface BookingService {

    BookingDto createBookingRequest(long userId, NewBookingRequestDto dtoRequest);

    BookingDto getBookingById(long userId, long bookingId);

    BookingDto updateBookingStatus(long userId, long bookingId, Boolean status);

    List<BookingDto> getBookingsByBookerId(long userId, State state, Integer from, Integer size);

    List<BookingDto> getBookingsByOwnerId(long userId, State state);
}