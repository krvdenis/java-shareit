package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDtoRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

    public static Booking mapToBooking(NewBookingDtoRequest dtoRequest, User user, Item item) {
        Booking booking = new Booking();
        booking.setStartTime(dtoRequest.getStart());
        booking.setEndTime(dtoRequest.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(user);
        booking.setItem(item);
        return booking;
    }

    public static BookingDto mapToBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStartTime());
        dto.setEnd(booking.getEndTime());
        dto.setItem(ItemMapper.mapToItemDto(booking.getItem()));
        dto.setBooker(UserMapper.mapToUserDto(booking.getBooker()));
        dto.setStatus(booking.getStatus());
        return dto;
    }
}