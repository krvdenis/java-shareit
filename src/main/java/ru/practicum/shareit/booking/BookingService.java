package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDtoRequest;

import java.util.List;

public interface BookingService {

    BookingDto createBookingRequest(Long userId, NewBookingDtoRequest dtoRequest);
    BookingDto getBookingById(Long userId, Long bookingId);
    BookingDto approveBookingStatus(Long userId, Long bookingId);
    BookingDto rejectBookingStatus(Long userId, Long bookingId);
    List<BookingDto> getBookingsByBookerId(Long userId, String state);
    List<BookingDto> getBookingsByOwnerId(Long userId, String state);
}
