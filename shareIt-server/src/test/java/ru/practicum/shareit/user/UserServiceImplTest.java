package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("Denis");
        userDto1.setEmail("denis@mail.ru");
        UserDto userDto2 = new UserDto();
        userDto2.setName("Ivan");
        userDto2.setEmail("Ivan@mail.ru");

        userService.createUser(userDto1);
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
}