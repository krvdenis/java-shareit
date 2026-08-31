package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.debug("Попытка зарегистрировать нового пользователя: {}", userDto);
        User user = UserMapper.mapToUser(userDto);
        try {
            userDto = UserMapper.mapToUserDto(repository.save(user));
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("email")) {
                throw new DuplicateDataException("Email уже занят");
            }
        }
        log.info("Пользователь {} успешно зарегистрирован под ID {}", userDto.getName(), userDto.getId());
        return userDto;
    }

    @Override
    @Transactional
    public UserDto patchUser(long userId, PatchUserRequest newUser) {
        log.debug("Попытка внести изменения в данные пользователя {}", newUser);
        User user = repository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId
                + " не найден"));
        UserMapper.patchUserFields(user, newUser);
        try {
            User patchedUser = repository.save(user);
            repository.flush();
            log.info("Данные пользователя с ID {} успешно обновлены", patchedUser.getId());
            return UserMapper.mapToUserDto(patchedUser);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("email")) {
                throw new DuplicateDataException("Email уже занят");
            }
            log.error("Произошла непредвиденная ошибка целостности данных при обновлении пользователя", e);
            throw new IllegalStateException("Произошла непредвиденная ошибка базы данных", e);
        }
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Попытка получить список всех пользователей");
        List<UserDto> usersDto = repository.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
        log.info("Отправлен список из {} пользователей", usersDto.size());

        return usersDto;
    }

    @Override
    public UserDto getUserById(long userId) {
        log.debug("Попытка получить пользователя: userId={}", userId);
        User user = repository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId
                + " не найден"));
        log.info("Отправлена информация о пользователе c ID: {}", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public void deleteUser(long userId) {
        log.debug("Попытка удаления пользователя с ID: {}", userId);
        repository.deleteById(userId);
        log.info("Пользователь с ID: {} - удалён.", userId);
    }
}