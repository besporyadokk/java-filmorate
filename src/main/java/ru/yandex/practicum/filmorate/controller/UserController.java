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
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("!! Добавление пользователя с пустым email");
            throw new ValidationException("Email не может быть пустым");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("!! Добавление пользователя с пустым логином");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("!! Добавление пользователя с неверной датой рождения");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.warn("!! Имя пользователя пустое - будет использован логин");
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
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("!! Обновление пользователя с некорректным email");
            throw new ValidationException("Email должен быть корректным адресом");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("!! Обновление пользователя с пустым email");
            throw new ValidationException("Email не может быть пустым");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("!! Обновление пользователя с пустым логином");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("!! Обновление пользователя с неверной датой рождения");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.warn("!! Имя пользователя пустое - будет использован логин");
        }
        usersMap.put(user.getId(), user);
        log.info("Пользователь успешно обновлён");
        return user;
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private Integer getNextId() {
        return nextId++;
    }
}
