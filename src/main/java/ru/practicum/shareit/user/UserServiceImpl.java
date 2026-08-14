package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto == null) {
            log.warn("Попытка добавить пользователя с пустыми данными");
            throw new ValidationException("Невозможно добавить пользователя с пустыми данными");
        }
        log.debug("Попытка зарегистрировать нового пользователя: {}", userDto);
        User user = UserMapper.mapToUser(userDto);
        userDto = UserMapper.mapToUserDto(userRepository.create(user));
        log.info("Пользователь {} успешно зарегистрирован под ID {}", userDto.getName(), userDto.getId());
        return userDto;
    }

    @Override
    public UserDto patchUser(long userId, PatchUserRequest newUser) {
        log.debug("Попытка внести изменения в данные пользователя {}", newUser);
        User oldUser = UserMapper.mapToUser(getUserById(userId));
        User patchedUser = userRepository.patch(UserMapper.patchUserFields(oldUser, newUser));
        log.info("Данные пользователя с ID {} успешно обновлены", patchedUser.getId());
        return UserMapper.mapToUserDto(patchedUser);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Попытка получить список всех пользователей");
        List<UserDto> usersDto = userRepository.getAll().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
        log.info("Отправлен список из {} пользователей", usersDto.size());

        return usersDto;
    }

    @Override
    public UserDto getUserById(long userId) {
        log.debug("Попытка получить пользователя: userId={}", userId);
        User user = userRepository.getById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId
                + " не найден"));
        log.info("Отправлена информация о пользователе c ID: {}", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public void deleteUser(long userId) {
        log.debug("Попытка удаления пользователя с ID: {}", userId);
        userRepository.delete(userId);
        log.info("Пользователь с ID: {} - удалён.", userId);
    }
}