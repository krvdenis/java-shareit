package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequestDto;
import ru.practicum.shareit.exception.BookingConflictException;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BookingServiceImplTest {
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private BookingService bookingService;
    private long userId;
    private long item1Id;
    private long wrongId;
    private NewBookingRequestDto newBooking1;
    private BookingDto bookingDto1;
    private BookingDto bookingDto2;
    private UserDto wrongUser;

    @BeforeEach
    void setUp() {
        UserDto newUser1 = new UserDto();
        newUser1.setName("Denis");
        newUser1.setEmail("denis@mail.ru");

        UserDto newUser2 = new UserDto();
        newUser2.setName("Irina");
        newUser2.setEmail("irina@mail.ru");
        wrongUser = userService.createUser(newUser2);

        UserDto createdUser = userService.createUser(newUser1);
        userId = createdUser.getId();

        NewItem newItem1 = new NewItem("Книга 1", "Описание 1", true, null);
        ItemDto createdItem1 = itemService.createItem(userId, newItem1);
        item1Id = createdItem1.getId();
        NewItem newItem2 = new NewItem("Книга 2", "Описание 2", true, null);
        ItemDto createdItem2 = itemService.createItem(userId, newItem2);
        long item2Id = createdItem2.getId();
        NewItem newItem3 = new NewItem("Книга 1", "Описание 1", false, null);
        ItemDto createdItem3 = itemService.createItem(userId, newItem3);
        wrongId = createdItem3.getId();

        newBooking1 = new NewBookingRequestDto(item1Id, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        NewBookingRequestDto newBooking2 = new NewBookingRequestDto(item2Id, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        bookingDto1 = bookingService.createBookingRequest(userId, newBooking1);
        bookingDto2 = bookingService.createBookingRequest(userId, newBooking2);
    }

    @Test
    void getBookingsByBookerId_whenStateWaiting_thenReturnedBookings() {
        List<BookingDto> bookings = bookingService.getBookingsByBookerId(userId, State.WAITING, 0, 10);

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getItem()).isNotNull();
        assertThat(bookings.get(1).getItem()).isNotNull();
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookings.get(1).getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookings.get(0).getBooker()).isNotNull();
        assertThat(bookings.get(1).getBooker()).isNotNull();
        assertThat(bookings.get(0).getId()).isNotNull();
        assertThat(bookings.get(1).getId()).isNotNull();
    }

    @Test
    void getBookingsByBookerId_whenStateUnknown_thenValidationExceptionThrown() {
        assertThrows(ValidationException.class,
                () -> bookingService.getBookingsByBookerId(userId, State.UNKNOWN, 0, 10));
    }

    @Test
    void getBookingsByOwnerId_whenStateWaiting_thenReturnedBookings() {
        List<BookingDto> bookings = bookingService.getBookingsByOwnerId(userId, State.WAITING);

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getItem()).isNotNull();
        assertThat(bookings.get(1).getItem()).isNotNull();
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookings.get(1).getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookings.get(0).getBooker()).isNotNull();
        assertThat(bookings.get(1).getBooker()).isNotNull();
        assertThat(bookings.get(0).getId()).isNotNull();
        assertThat(bookings.get(1).getId()).isNotNull();
    }

    @Test
    void getBookingsByOwnerId_whenStateCurrent_thenReturnedEmptyList() {
        List<BookingDto> bookings = bookingService.getBookingsByOwnerId(userId, State.CURRENT);

        assertThat(bookings).hasSize(0);
    }

    @Test
    void getBookingsByOwnerId_whenStatePast_thenReturnedEmptyList() {
        bookingService.updateBookingStatus(userId, bookingDto1.getId(), true);
        bookingService.updateBookingStatus(userId, bookingDto2.getId(), true);
        List<BookingDto> bookings = bookingService.getBookingsByOwnerId(userId, State.PAST);

        assertThat(bookings).hasSize(0);
    }

    @Test
    void getBookingsByOwnerId_whenStateFuture_thenReturnedBookings() {
        bookingService.updateBookingStatus(userId, bookingDto1.getId(), true);
        bookingService.updateBookingStatus(userId, bookingDto2.getId(), true);
        List<BookingDto> bookings = bookingService.getBookingsByOwnerId(userId, State.FUTURE);

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getItem()).isNotNull();
        assertThat(bookings.get(1).getItem()).isNotNull();
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookings.get(1).getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookings.get(0).getBooker()).isNotNull();
        assertThat(bookings.get(1).getBooker()).isNotNull();
        assertThat(bookings.get(0).getId()).isNotNull();
        assertThat(bookings.get(1).getId()).isNotNull();
    }

    @Test
    void getBookingsByOwnerId_whenStateRejected_thenReturnedBookings() {
        bookingService.updateBookingStatus(userId, bookingDto1.getId(), false);
        bookingService.updateBookingStatus(userId, bookingDto2.getId(), false);

        List<BookingDto> bookings = bookingService.getBookingsByOwnerId(userId, State.REJECTED);

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getItem()).isNotNull();
        assertThat(bookings.get(1).getItem()).isNotNull();
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(bookings.get(1).getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(bookings.get(0).getBooker()).isNotNull();
        assertThat(bookings.get(1).getBooker()).isNotNull();
        assertThat(bookings.get(0).getId()).isNotNull();
        assertThat(bookings.get(1).getId()).isNotNull();
    }

    @Test
    void getBookingsByBookerId_whenStateAll_thenReturnedBookings() {
        List<BookingDto> bookings = bookingService.getBookingsByBookerId(userId, State.ALL, 0, 10);

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getItem()).isNotNull();
        assertThat(bookings.get(1).getItem()).isNotNull();
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookings.get(1).getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookings.get(0).getBooker()).isNotNull();
        assertThat(bookings.get(1).getBooker()).isNotNull();
        assertThat(bookings.get(0).getId()).isNotNull();
        assertThat(bookings.get(1).getId()).isNotNull();
    }

    @Test
    void getBookingsByBookerId_whenStateCurrent_thenReturnedEmptyList() {
        List<BookingDto> bookings = bookingService.getBookingsByBookerId(userId, State.CURRENT, 0, 10);

        assertThat(bookings).hasSize(0);
    }

    @Test
    void getBookingsByBookerId_whenStatePast_thenReturnedEmptyList() {
        bookingService.updateBookingStatus(userId, bookingDto1.getId(), true);
        bookingService.updateBookingStatus(userId, bookingDto2.getId(), true);
        List<BookingDto> bookings = bookingService.getBookingsByBookerId(userId, State.PAST, 0, 10);

        assertThat(bookings).hasSize(0);
    }

    @Test
    void getBookingsByBookerId_whenStateFuture_thenReturnedBookings() {
        bookingService.updateBookingStatus(userId, bookingDto1.getId(), true);
        bookingService.updateBookingStatus(userId, bookingDto2.getId(), true);
        List<BookingDto> bookings = bookingService.getBookingsByBookerId(userId, State.FUTURE, 0, 10);

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getItem()).isNotNull();
        assertThat(bookings.get(1).getItem()).isNotNull();
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookings.get(1).getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookings.get(0).getBooker()).isNotNull();
        assertThat(bookings.get(1).getBooker()).isNotNull();
        assertThat(bookings.get(0).getId()).isNotNull();
        assertThat(bookings.get(1).getId()).isNotNull();
    }

    @Test
    void getBookingsByBookerId_whenStateREJECTED_thenReturnedBookings() {
        bookingService.updateBookingStatus(userId, bookingDto1.getId(), false);
        bookingService.updateBookingStatus(userId, bookingDto2.getId(), false);

        List<BookingDto> bookings = bookingService.getBookingsByBookerId(userId, State.REJECTED, 0, 10);

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getItem()).isNotNull();
        assertThat(bookings.get(1).getItem()).isNotNull();
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(bookings.get(1).getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(bookings.get(0).getBooker()).isNotNull();
        assertThat(bookings.get(1).getBooker()).isNotNull();
        assertThat(bookings.get(0).getId()).isNotNull();
        assertThat(bookings.get(1).getId()).isNotNull();
    }

    @Test
    void createBookingRequest_whenItemAvailable_thenReturnedBooking() {
        NewBookingRequestDto newBooking = newBooking1 = new NewBookingRequestDto(item1Id,
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4));

        BookingDto actualBooking = bookingService.createBookingRequest(userId, newBooking);

        assertThat(actualBooking.getId()).isNotNull();
        assertThat(actualBooking.getStart()).isEqualTo(newBooking.getStart());
        assertThat(actualBooking.getEnd()).isEqualTo(newBooking.getEnd());
    }

    @Test
    void createBookingRequest_whenItemNotAvailable_thenReturnedBooking() {
        NewBookingRequestDto newBooking = newBooking1 = new NewBookingRequestDto(wrongId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThrows(BookingConflictException.class, () -> bookingService.createBookingRequest(userId, newBooking));
    }

    @Test
    void createBookingRequest_whenStartEqualsEnd_thenValidationExceptionThrown() {
        LocalDateTime now = LocalDateTime.now();
        NewBookingRequestDto newBooking = newBooking1 = new NewBookingRequestDto(wrongId,
                now,
                now);

        assertThrows(ValidationException.class, () -> bookingService.createBookingRequest(userId, newBooking));
    }

    @Test
    void createBookingRequest_whenEndBeforeStart_thenValidationExceptionThrown() {
        NewBookingRequestDto newBooking = newBooking1 = new NewBookingRequestDto(wrongId,
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.createBookingRequest(userId, newBooking));
    }

    @Test
    void getBookingById_whenInvokedOwnerOrBooker_thenReturnedBooking() {
        long bookingId = bookingDto1.getId();

        BookingDto actualBooking = bookingService.getBookingById(userId, bookingId);

        assertThat(actualBooking.getId()).isEqualTo(bookingDto1.getId());
        assertThat(actualBooking.getStart()).isEqualTo(bookingDto1.getStart());
        assertThat(actualBooking.getEnd()).isEqualTo(bookingDto1.getEnd());
    }

    @Test
    void getBookingById_whenInvokedNotOwnerAndNotBooker_thenNoAccessExceptionThrown() {
        assertThrows(NoAccessException.class, () -> bookingService.updateBookingStatus(wrongUser.getId(),
                bookingDto1.getId(), true));
    }

    @Test
    void getBookingsByOwnerId_whenStateAll_thenReturnedBookings() {
        List<BookingDto> bookings = bookingService.getBookingsByOwnerId(userId, State.ALL);

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getItem()).isNotNull();
        assertThat(bookings.get(1).getItem()).isNotNull();
        assertThat(bookings.get(0).getBooker()).isNotNull();
        assertThat(bookings.get(1).getBooker()).isNotNull();
        assertThat(bookings.get(0).getId()).isNotNull();
        assertThat(bookings.get(1).getId()).isNotNull();
    }

    @Test
    void updateBookingStatus_whenInvokedOwnerAndStatusTrue_thenReturnedBooking() {
        BookingStatus expectedBookingStatus = BookingStatus.APPROVED;

        BookingDto actualBooking = bookingService.updateBookingStatus(userId, bookingDto1.getId(), true);

        assertThat(actualBooking.getId()).isEqualTo(bookingDto1.getId());
        assertThat(actualBooking.getStatus()).isEqualTo(expectedBookingStatus);
    }

    @Test
    void updateBookingStatus_whenInvokedOwnerAndStatusFalse_thenReturnedBooking() {
        BookingStatus expectedBookingStatus = BookingStatus.REJECTED;

        BookingDto actualBooking = bookingService.updateBookingStatus(userId, bookingDto1.getId(), false);

        assertThat(actualBooking.getId()).isEqualTo(bookingDto1.getId());
        assertThat(actualBooking.getStatus()).isEqualTo(expectedBookingStatus);
    }

    @Test
    void updateBookingStatus_whenInvokedNotOwner_thenNoAccessExceptionThrown() {
        assertThrows(NoAccessException.class, () -> bookingService.updateBookingStatus(wrongUser.getId(),
                bookingDto1.getId(), true));
    }
}