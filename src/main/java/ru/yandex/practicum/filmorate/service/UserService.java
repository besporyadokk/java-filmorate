package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FriendAddingException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {


    private final UserStorage userStorage;

    public Collection<User> getUsers() {
        return new ArrayList<>(userStorage.getUsers().values());
    }

    public User addUser(User user) {
        log.info("Обрабатывается запрос добавления пользователя");
        validateUser(user);

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

    public User getUser(Integer id) {
        User user = userStorage.getUser(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }


    public User addFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        if (user.getFriends().containsKey(friendId)) {
            throw new FriendAddingException("Пользователь с айди " + userId + " уже в друзьях у пользователя " + friendId);
        }


        user.addFriend(friendId, FriendStatus.UNCONFIRMED);
        userStorage.updateUser(userId, user);

        log.info("Пользователь {} добавил в друзья пользователя {} ", userId, friendId);
        return user;
    }

    public User removeFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);


        user.deleteFriend(friendId);
        userStorage.updateUser(userId, user);

        log.info("Пользователь {} удалил из друзей пользователя {} ", userId, friendId);
        return user;
    }

    public Set<User> getFriends(Integer userId) {
        User user = getUser(userId);

        return user.getFriends().keySet().stream()
                .map(userStorage::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Integer id, Integer otherId) {
        User user = getUser(id);
        User otherUser = getUser(otherId);

        Set<Integer> commonFriendIds = new HashSet<>(user.getFriends().keySet());
        commonFriendIds.retainAll(otherUser.getFriends().keySet());

        return commonFriendIds.stream()
                .map(userStorage::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }


    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("!! Пользователь с пустым email");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) { // Упростить проверку
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


}