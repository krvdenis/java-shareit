package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicateDataException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserDaoImpl implements UserDao {
    private final AtomicLong nextId = new AtomicLong(1);
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        if (isEmailAlreadyExist(user.getEmail())) {
            throw new DuplicateDataException("Данная электронная почта уже используется: " + user.getEmail());
        }
        user.setId(nextId.getAndIncrement());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User patch(User newUser) {
        User oldUser = users.get(newUser.getId());

        if (newUser.getName() != null && !newUser.getName().equals(oldUser.getName())) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getEmail() != null && !Objects.equals(oldUser.getEmail(), newUser.getEmail())) {
            if (isEmailAlreadyExist(newUser.getEmail())) {
                throw new DuplicateDataException("Данная электронная почта уже используется: " + newUser.getEmail());
            }
            oldUser.setEmail(newUser.getEmail());
        }
        return oldUser;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void delete(long userId) {
        users.keySet().removeIf(key -> Objects.equals(key, userId));
    }

    private boolean isEmailAlreadyExist(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }
}