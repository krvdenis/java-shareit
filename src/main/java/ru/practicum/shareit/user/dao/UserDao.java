package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    User create(User user);

    User patch(User user);

    List<User> getAll();

    Optional<User> getById(long userId);

    void delete(long userId);
}