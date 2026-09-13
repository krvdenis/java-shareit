package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;
    private UserDto userDto1;

    @BeforeEach
    void setUp() {
        UserDto dto = new UserDto();
        dto.setName("Denis");
        dto.setEmail("denis@mail.ru");
        userDto1 = userService.createUser(dto);
    }

    @Test
    void getUsers_whenInvoked_thenReturnedUsers() {
        createUser("Ivan", "Ivan@mail.ru");

        List<UserDto> users = userService.getUsers();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("Denis");
        assertThat(users.get(1).getName()).isEqualTo("Ivan");
        assertThat(users.get(0).getId()).isNotNull();
        assertThat(users.get(1).getId()).isNotNull();
    }

    @Test
    void createUser_whenInvoked_thenReturnedUser() {
        UserDto expectedUser = new UserDto();
        expectedUser.setName("Irina");
        expectedUser.setEmail("Irina@mail.ru");
        UserDto actualUser = userService.createUser(expectedUser);

        assertThat(actualUser.getName()).isEqualTo(expectedUser.getName());
        assertThat(actualUser.getEmail()).isEqualTo(expectedUser.getEmail());
        assertThat(actualUser.getId()).isNotNull();
    }

    @Test
    void getUserById_whenUserExists_thenReturnedUser() {
        long userId = userDto1.getId();
        UserDto actualUser = userService.getUserById(userId);

        assertThat(actualUser.getName()).isEqualTo(userDto1.getName());
        assertThat(actualUser.getEmail()).isEqualTo(userDto1.getEmail());
        assertThat(actualUser.getId()).isEqualTo(userDto1.getId());
    }

    @Test
    void getUserById_whenUserNotExists_thenReturnedUser() {
        long wrongId = 6;

        assertThrows(NotFoundException.class, () -> userService.getUserById(wrongId));
    }

    @Test
    void deleteUser_whenInvoked_thenUserNotFound() {
        long userId = userDto1.getId();
        userService.deleteUser(userId);

        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void patchUser_whenInvoked_thenReturnedNewUser() {
        PatchUserRequest patchUserRequest = new PatchUserRequest("Irina", "irina@mail.ru");
        UserDto actualUser = userService.patchUser(userDto1.getId(), patchUserRequest);

        assertThat(actualUser.getName()).isEqualTo(patchUserRequest.getName());
        assertThat(actualUser.getEmail()).isEqualTo(patchUserRequest.getEmail());
        assertThat(actualUser.getId()).isNotNull();
    }

    private void createUser(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        userService.createUser(dto);
    }
}