package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequestDto;
import ru.practicum.shareit.exception.BookingConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private static final String API_PREFIX = "/bookings";
    private static final long User_ID = 1;
    private static final long BOOKING_ID = 1;
    private static NewBookingRequestDto request;
    private static BookingDto response;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private BookingService bookingService;
    @Autowired
    private MockMvc mvc;

    @BeforeAll
    static void setUp() {
        request = new NewBookingRequestDto(1L,
                LocalDateTime.of(26, 12, 12, 12, 12),
                LocalDateTime.of(27, 1, 1, 1, 1));
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("adsad");
        item.setDescription("dasdasdwd");
        item.setAvailable(true);
        UserDto booker = new UserDto(
                1L,
                "asdadasd",
                "asdadasd.mail.ru");
        response = new BookingDto();
        response.setId(1L);
        response.setStart(request.getStart());
        response.setEnd(request.getEnd());
        response.setItem(item);
        response.setBooker(booker);
        response.setStatus(BookingStatus.WAITING);
    }

    @Test
    void bookItem_whenItemAvailableAndNoActiveBookingForThatTime_thenReturnedBooking() throws Exception {
        when(bookingService.createBookingRequest(anyLong(), any()))
                .thenReturn(response);

        mvc.perform(post(API_PREFIX)
                        .header(X_SHARER_USER_ID, User_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.status", is(response.getStatus().name()), Boolean.class));
    }

    @Test
    void bookItem_whenItemAvailableAndExistsActiveBookingForThatTime_thenBookingConflictExceptionThrown() throws Exception {
        when(bookingService.createBookingRequest(anyLong(), any()))
                .thenThrow(new BookingConflictException("На это время уже есть бронь"));

        mvc.perform(post(API_PREFIX)
                        .header(X_SHARER_USER_ID, User_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookItem_whenItemNotAvailable_thenBookingConflictExceptionThrown() throws Exception {
        when(bookingService.createBookingRequest(anyLong(), any()))
                .thenThrow(new BookingConflictException("Данная вещь недоступна для бронирования"));

        mvc.perform(post(API_PREFIX)
                        .header(X_SHARER_USER_ID, User_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_whenOwnerInvoked_thenReturnedUpdatedBooking() throws Exception {
        when(bookingService.updateBookingStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(response);

        mvc.perform(patch(API_PREFIX + "/{bookingId}?approved={approved}", BOOKING_ID, true)
                        .header(X_SHARER_USER_ID, User_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.status", is(response.getStatus().name()), Boolean.class));
    }

    @Test
    void getById_whenBookingFound_thenReturnedBooking() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(response);

        mvc.perform(get(API_PREFIX + "/{bookingId}", BOOKING_ID)
                        .header(X_SHARER_USER_ID, User_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.status", is(response.getStatus().name()), Boolean.class));
    }

    @Test
    void getById_whenBookingNotFound_thenNotFoundExceptionThrown() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Бронь не найдена"));

        mvc.perform(get(API_PREFIX + "/{bookingId}", BOOKING_ID)
                        .header(X_SHARER_USER_ID, User_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsByBookerId_whenBookerExists_thenReturnEmptyList() throws Exception {
        when(bookingService.getBookingsByBookerId(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(API_PREFIX + "?state={state}&from={from}&size={size}", State.ALL, 0, 10)
                        .header(X_SHARER_USER_ID, User_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void getBookingsByBookerId_whenBookerNotExists_thenNotFoundExceptionThrown() throws Exception {
        when(bookingService.getBookingsByBookerId(anyLong(), any(), anyInt(), anyInt()))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mvc.perform(get(API_PREFIX + "?state={state}&from={from}&size={size}", State.ALL, 0, 10)
                        .header(X_SHARER_USER_ID, User_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllByOwnerId_wheInvoked_thenReturnedEmptyList() throws Exception {
        when(bookingService.getBookingsByOwnerId(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(API_PREFIX + "?state={state}", State.ALL)
                        .header(X_SHARER_USER_ID, User_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }
}