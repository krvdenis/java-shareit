package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto patchUser(Long userId, PatchUserRequest newUser);

    List<UserDto> getAllUsers();

    UserDto getUserById(Long userId);

    void deleteUser(Long userId);
}