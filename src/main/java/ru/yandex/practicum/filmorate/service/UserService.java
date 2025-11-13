package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FriendAddingException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserService {
    @Autowired
    private UserStorage userStorage;
    private int nextId = 1;

    public Collection<User> getUsers() {
        return new ArrayList<>(userStorage.getUsers().values());
    }

    public User addUser(User user) {
        log.info("Обрабатывается запрос добавления пользователя");
        validateUser(user);

        user.setId(getNextId());
        userStorage.addUser(user);
        log.info("Пользователь успешно добавлен");
        return user;
    }

    public User updateUser(User user) {
        log.info("Обрабатывается запрос обновления пользователя");
        if (!userStorage.getUsers().containsKey(user.getId())) {
            log.warn("!! Попытка обновления несуществующего пользователя с id: {}", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        validateUser(user);

        userStorage.updateUser(user.getId(), user);
        log.info("Пользователь успешно обновлён");
        return user;
    }

    public User addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);

        if (user == null) {
            throw new NotFoundException("Пользователя с айди " + userId + " не найдено");
        }

        if (friend == null) {
            throw new NotFoundException("Пользователя с айди " + friendId + " не найдено");
        }

        if (user.getFriends().contains(friendId) || friend.getFriends().contains(userId)) {
            throw new FriendAddingException("Пользователи с айди " + userId + " и " + friendId +
                    " уже в друзьях друг у друга");
        }


        Set<Integer> userFriends = new HashSet<>(user.getFriends());
        userFriends.add(friendId);
        user.setFriends(userFriends);
        userStorage.updateUser(userId, user);

        Set<Integer> friendFriends = new HashSet<>(friend.getFriends());
        friendFriends.add(userId);
        friend.setFriends(friendFriends);
        userStorage.updateUser(friendId, friend);

        log.info("Пользователь {} добавил в друзья пользователя {} ", userId, friendId);

        return user;
    }

    public User removeFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);

        if (user == null) {
            throw new NotFoundException("Пользователя с айди " + userId + " не найдено");
        }

        if (friend == null) {
            throw new NotFoundException("Пользователя с айди " + friendId + " не найдено");
        }


        Set<Integer> userFriends = new HashSet<>(user.getFriends());
        userFriends.remove(friendId);
        user.setFriends(userFriends);
        userStorage.updateUser(userId, user);

        Set<Integer> friendFriends = new HashSet<>(friend.getFriends());
        friendFriends.remove(userId);
        friend.setFriends(friendFriends);
        userStorage.updateUser(friendId, friend);

        log.info("Пользователь {} удалил из друзей пользователя {} ", userId, friendId);

        return user;
    }

    public Set<User> getFriends(Integer userId) {
        User user = userStorage.getUser(userId);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        return user.getFriends().stream()
                .map(userStorage::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Integer id, Integer otherId) {
        User user = userStorage.getUser(id);
        User otherUser = userStorage.getUser(otherId);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        if (otherUser == null) {
            throw new NotFoundException("Пользователь с id " + otherId + " не найден");
        }

        Set<Integer> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());

        return commonFriendIds.stream()
                .map(userStorage::getUser)
                .collect(Collectors.toSet());
    }

    private void validateUser(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("!! Пользователь с пустым email");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@") || !user.getEmail().contains(".")) {
            log.warn("!! Пользователь с некорректным email");
            throw new ValidationException("Email должен быть корректным адресом");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("!! Пользователь с пустым логином");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("!! Пользователь с неверной датой рождения");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.warn("!! Имя пользователя пустое - будет использован логин");
        }
    }

    private Integer getNextId() {
        return nextId++;
    }
}
