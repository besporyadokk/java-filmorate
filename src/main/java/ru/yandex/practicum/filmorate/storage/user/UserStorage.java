package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;

public interface UserStorage {
    Map<Integer, User> getUsers();

    User getUser(Integer id);

    User addUser(User user);

    User updateUser(Integer id, User user);

    void deleteUser(Integer id);
}