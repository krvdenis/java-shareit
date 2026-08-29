package ru.practicum.shareit.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public static User mapToUser(UserDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setId(dto.getId());
        return user;
    }

    public static User patchUserFields(User user, PatchUserRequest patchUserRequest) {
        if (patchUserRequest.hasUserName()) {
            user.setName(patchUserRequest.getName());
        }
        if (patchUserRequest.hasUserEmail()) {
            user.setEmail(patchUserRequest.getEmail());
        }
        return user;
    }
}