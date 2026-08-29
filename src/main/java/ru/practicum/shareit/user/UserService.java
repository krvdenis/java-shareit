package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto patchUser(long userId, PatchUserRequest newUser);

    List<UserDto> getAllUsers();

    UserDto getUserById(long userId);

    void deleteUser(long userId);

}