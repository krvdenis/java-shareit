package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

    public static Booking mapToBooking(NewBookingRequestDto dto, User user, Item item) {
        Booking booking = new Booking();
        LocalDateTime rawStart = dto.getStart();
        LocalDateTime rawEnd = dto.getEnd();
        ZonedDateTime moscowStart = rawStart.atZone(ZoneId.of("Europe/Moscow"));
        ZonedDateTime moscowEnd = rawEnd.atZone(ZoneId.of("Europe/Moscow"));
        ZonedDateTime utcStart = moscowStart.withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime utcEnd = moscowEnd.withZoneSameInstant(ZoneId.of("UTC"));
        booking.setStartTime(utcStart.toLocalDateTime());
        booking.setEndTime(utcEnd.toLocalDateTime());
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(user);
        booking.setItem(item);
        return booking;
    }

    public static BookingDto mapToBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        LocalDateTime rawStart = booking.getStartTime();
        LocalDateTime rawEnd = booking.getEndTime();
        ZonedDateTime utcStart = rawStart.atZone(ZoneId.of("UTC"));
        ZonedDateTime utcEnd = rawEnd.atZone(ZoneId.of("UTC"));
        ZonedDateTime moscowStart = utcStart.withZoneSameInstant(ZoneId.of("Europe/Moscow"));
        ZonedDateTime moscowEnd = utcEnd.withZoneSameInstant(ZoneId.of("Europe/Moscow"));
        dto.setStart(moscowStart.toLocalDateTime());
        dto.setEnd(moscowEnd.toLocalDateTime());
        dto.setItem(ItemMapper.mapToItemDto(booking.getItem()));
        dto.setBooker(UserMapper.mapToUserDto(booking.getBooker()));
        dto.setStatus(booking.getStatus());
        return dto;
    }
}