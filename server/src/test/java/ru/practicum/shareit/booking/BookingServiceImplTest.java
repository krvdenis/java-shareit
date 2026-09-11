package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequestDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BookingServiceImplTest {
    @Autowired
    UserService userService;
    @Autowired
    ItemService itemService;
    @Autowired
    BookingService bookingService;
    long userId;
    long item1Id;
    long item2Id;

    @BeforeEach
    void setUp() {
        UserDto userDto = new UserDto();
        userDto.setName("Denis");
        userDto.setEmail("denis@mail.ru");

        UserDto createdUser = userService.createUser(userDto);
        userId = createdUser.getId();

        NewItem newItem1 = new NewItem("Книга 1", "Описание 1", true, null);
        ItemDto createdItem1 = itemService.createItem(userId, newItem1);
        item1Id = createdItem1.getId();
        NewItem newItem2 = new NewItem("Книга 2", "Описание 2", true, null);
        ItemDto createdItem2 = itemService.createItem(userId, newItem2);
        item2Id = createdItem2.getId();
    }

    @Test
    void getBookingsByBookerId_whenInvoked_thenReturnedBookings() {
        NewBookingRequestDto newBooking1;
        newBooking1 = new NewBookingRequestDto(item1Id,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        NewBookingRequestDto newBooking2;
        newBooking2 = new NewBookingRequestDto(item2Id,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        bookingService.createBookingRequest(userId, newBooking1);
        bookingService.createBookingRequest(userId, newBooking2);

        List<BookingDto> bookings = bookingService.getBookingsByBookerId(userId, State.ALL, 0, 10);

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getItem()).isNotNull();
        assertThat(bookings.get(1).getItem()).isNotNull();
        assertThat(bookings.get(0).getBooker()).isNotNull();
        assertThat(bookings.get(1).getBooker()).isNotNull();
        assertThat(bookings.get(0).getId()).isNotNull();
        assertThat(bookings.get(1).getId()).isNotNull();
    }
}