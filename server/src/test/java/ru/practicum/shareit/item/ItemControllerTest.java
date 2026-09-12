package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.item.dto.PatchItem;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.ApiHeaders.X_SHARER_USER_ID;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String API_PREFIX = "/items";
    private static final long USER_ID = 1;
    private static final long ITEM_ID = 1;
    private static ItemDto response;
    private static NewItem request;
    private static NewCommentDto requestComment;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemService itemService;
    @Autowired
    private MockMvc mvc;

    @BeforeAll
    static void setUp() {
        request = new NewItem("adsad", "dasdasdwd", false, null);
        response = new ItemDto();
        response.setId(1L);
        response.setName(request.getName());
        response.setDescription(request.getDescription());
        response.setAvailable(request.getAvailable());
        requestComment = new NewCommentDto("asdad");

    }

    @Test
    void createItem_whenUserExists_thenReturnedItemRequest() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenReturn(response);

        mvc.perform(post(API_PREFIX)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(response.getName()), String.class))
                .andExpect(jsonPath("$.description", is(response.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(response.getAvailable())));
    }

    @Test
    void createItem_whenUserNotExists_thenNotFoundExceptionThrown() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mvc.perform(post(API_PREFIX)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCommentForItem_whenBookingComplete_thenReturnedComment() throws Exception {
        CommentDto response = new CommentDto();
        response.setId(1L);
        response.setText(requestComment.getText());
        response.setItemId(1L);
        response.setAuthorName("Adasd");
        response.setCreated(LocalDateTime.now());

        when(itemService.createCommentForItem(anyLong(), anyLong(), any()))
                .thenReturn(response);

        mvc.perform(post(API_PREFIX + "/{itemId}/comment", 1)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(mapper.writeValueAsString(requestComment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(response.getText()), String.class))
                .andExpect(jsonPath("$.itemId", is(response.getItemId()), Long.class))
                .andExpect(jsonPath("$.authorName", is(response.getAuthorName()), String.class))
                .andExpect(jsonPath("$.created", notNullValue()));
    }

    @Test
    void createCommentForItem_whenBookingNotExist_thenResponseStatusExceptionThrown() throws Exception {
        when(itemService.createCommentForItem(anyLong(), anyLong(), any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Пользователь не может оставить отзыв, так как ранее не арендовал эту вещь."));

        mvc.perform(post(API_PREFIX + "/{itemId}/comment", 1)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(mapper.writeValueAsString(requestComment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patch_WhenItemFound_thenReturnedUpdatedItem() throws Exception {
        PatchItem patchItem = new PatchItem("ads", "asdad", true);


        when(itemService.patchItem(anyLong(), anyLong(), any()))
                .thenReturn(response);

        mvc.perform(patch(API_PREFIX + "/{itemId}", ITEM_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(mapper.writeValueAsString(patchItem))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(response.getName()), String.class))
                .andExpect(jsonPath("$.description", is(response.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(response.getAvailable())));
    }

    @Test
    void getById_whenItemFound_thenReturnedItem() throws Exception {
        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("adasd");
        responseDto.setDescription("asdasdw");
        responseDto.setAvailable(true);

        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mvc.perform(get(API_PREFIX + "/{itemId}", ITEM_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(responseDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(responseDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(responseDto.getAvailable())));
    }

    @Test
    void getById_whenUserNotFound_thenNotFoundExceptionThrown() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mvc.perform(get(API_PREFIX + "/{itemId}", ITEM_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_whenInvoked_thenReturnedEmptyList() throws Exception {
        when(itemService.getUserItems(anyLong()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/items")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void searchByText_whenMatchesNotFound_thenReturnedEmptyList() throws Exception {
        when(itemService.searchItemByText(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(API_PREFIX + "/search?text={text}", "text")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }
}