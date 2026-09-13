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
        userDto1 = new UserDto();
        userDto1.setName("Denis");
        userDto1.setEmail("denis@mail.ru");
        UserDto userDto2 = new UserDto();
        userDto2.setName("Ivan");
        userDto2.setEmail("Ivan@mail.ru");

        userDto1 = userService.createUser(userDto1);
        userService.createUser(userDto2);
    }

    @Test
    void getUsers_whenInvoked_thenReturnedUsers() {
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
}