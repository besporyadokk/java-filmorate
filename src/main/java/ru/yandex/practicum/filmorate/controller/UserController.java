package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private Map<Integer, User> usersMap = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public Collection<User> findAll() {
        return usersMap.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Обрабатывается запрос добавления пользователя");
        validateUser(user);

        user.setId(getNextId());
        usersMap.put(user.getId(), user);
        log.info("Пользователь успешно добавлен");
        return user;
    }

        user.setId(getNextId());
        usersMap.put(user.getId(), user);
        log.info("Пользователь успешно добавлен");
        return user;
    }


    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Обрабатывается запрос обновления пользователя");
        if (!usersMap.containsKey(user.getId())) {
            log.warn("!! Попытка обновления несуществующего пользователя с id: {}", user.getId());
            throw new ValidationException("Пользователь с id " + user.getId() + " не найден");
        }
        validateUser(user);

        usersMap.put(user.getId(), user);
        log.info("Пользователь успешно обновлён");
        return user;
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
