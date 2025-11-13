package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;

public interface UserStorage {

    void addUser(User user);

    User getUser(Integer id);

    Map<Integer, User> getUsers();

    void updateUser(Integer id, User user);

    void deleteUser(Integer id);
}
