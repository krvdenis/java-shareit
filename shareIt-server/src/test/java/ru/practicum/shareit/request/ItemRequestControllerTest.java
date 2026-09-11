package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private static final String API_PREFIX = "/requests";
    private static final long USER_ID = 1;
    private static final long ITEM_REQUEST_ID = 1;
    private static NewItemRequest request;
    private static ItemRequestDto response;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mvc;

    @BeforeAll
    static void setUp() {
        request = new NewItemRequest("asdadasd");
        response = new ItemRequestDto(1L, "asdadasd", LocalDateTime.now());
    }

    @Test
    void createItemRequest_whenUserExists_thenReturnedItemRequest() throws Exception {
        when(itemRequestService.createItemRequest(anyLong(), any()))
                .thenReturn(response);

        mvc.perform(post(API_PREFIX)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(response.getDescription()), String.class))
                .andExpect(jsonPath("$.created", notNullValue()));
    }

    @Test
    void createItemRequest_whenUserNotExists_thenNotFoundExceptionThrown() throws Exception {
        when(itemRequestService.createItemRequest(anyLong(), any()))
                .thenAnswer(invocation -> {
                    long userId = invocation.getArgument(0, Long.class);
                    throw new NotFoundException("Пользователь с ID %d не найден".formatted(userId));
                });

        mvc.perform(post(API_PREFIX)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь с ID 1 не найден")));
    }

    @Test
    void getItemRequestsByRequesterId_whenInvoked_thenReturnedEmptyList() throws Exception {
        when(itemRequestService.getItemRequestsByRequesterId(anyLong()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(API_PREFIX)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getRequestsFromOthers_whenInvoked_thenReturnedEmptyList() throws Exception {
        when(itemRequestService.getRequestsFromOthers(anyLong()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(API_PREFIX + "/all")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void getItemRequestById_whenItemRequestFound_thenReturnedItemRequest() throws Exception {
        ItemRequestDetailedDto response = new ItemRequestDetailedDto(
                ITEM_REQUEST_ID,
                "asdsad",
                LocalDateTime.now(),
                Collections.emptyList());

        when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(response);

        mvc.perform(get(API_PREFIX + "/{requestId}", ITEM_REQUEST_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId().intValue())))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andExpect(jsonPath("$.items.length()", is(0)));
    }

    @Test
    void getItemRequestById_whenItemRequestNotFound_thenNotFoundExceptionThrown() throws Exception {
        when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mvc.perform(get(API_PREFIX + "/{requesterId}", ITEM_REQUEST_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}