package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    private static final String API_PREFIX = "/users";
    private static final long USER_ID = 1;
    private static UserDto request;
    private static UserDto response;
    private static PatchUserRequest patchUserRequest;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mvc;

    @BeforeAll
    static void setUp() {
        request = new UserDto();
        request.setName("asdadasd");
        request.setEmail("asdadasd.mail.ru");
        response = new UserDto(
                1L,
                "asdadasd",
                "asdadasd.mail.ru");
        patchUserRequest = new PatchUserRequest("denis", "denis@mail.ru");
    }

    @Test
    void create_whenInvoked_thenReturnedUser() throws Exception {
        when(userService.createUser(any()))
                .thenReturn(response);

        mvc.perform(post(API_PREFIX)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(response.getName()), String.class))
                .andExpect(jsonPath("$.email", is(response.getEmail()), String.class));
    }

    @Test
    void patch_WhenUserFound_thenReturnedUpdatedUser() throws Exception {
        response.setName(patchUserRequest.getName());
        response.setEmail(patchUserRequest.getEmail());

        when(userService.patchUser(anyLong(), any()))
                .thenReturn(response);

        mvc.perform(patch(API_PREFIX + "/{userId}", USER_ID)
                        .content(mapper.writeValueAsString(patchUserRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(response.getName()), String.class))
                .andExpect(jsonPath("$.email", is(response.getEmail()), String.class));
    }

    @Test
    void patch_WhenUserNotFound_thenNotFoundExceptionThrown() throws Exception {
        when(userService.patchUser(anyLong(), any()))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mvc.perform(patch(API_PREFIX + "/{userId}", USER_ID)
                        .content(mapper.writeValueAsString(patchUserRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_whenInvoked_thenReturnedEmptyList() throws Exception {
        when(userService.getUsers())
                .thenReturn(Collections.emptyList());

        mvc.perform(get(API_PREFIX)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void getById_whenUserFound_thenReturnedUser() throws Exception {
        when(userService.getUserById(USER_ID))
                .thenReturn(response);

        mvc.perform(get(API_PREFIX + "/{userId}", USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId().intValue())))
                .andExpect(jsonPath("$.name", is(response.getName())))
                .andExpect(jsonPath("$.email", notNullValue()));
    }

    @Test
    void getById_whenUserNotFound_thenNotFoundExceptionThrown() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mvc.perform(get(API_PREFIX + "/{userId}", USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_whenInvoked_thenResponseStatusNoContentAndReturnVoid() throws Exception {
        mvc.perform(delete(API_PREFIX + "/{userId}", USER_ID))
                .andExpect(status().isNoContent());
    }
}