package ru.yandex.practicum.filmorate.storage.user;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users;

    public InMemoryUserStorage() {
        users = new HashMap<>();
    }

    @Override
    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public User getUser(Integer id) {
        return users.get(id);
    }

    @Override
    public Map<Integer, User> getUsers() {
        return users;
    }

    @Override
    public void updateUser(Integer id, User user) {
        User userToUpdate = users.get(id);
        if (userToUpdate != null) {
            userToUpdate.setName(user.getName());
            userToUpdate.setBirthday(user.getBirthday());
            userToUpdate.setLogin(user.getLogin());
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setFriends(user.getFriends());
        }
    }

}
