package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    // Тесты для email
    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        try {
            userController.create(user);
        } catch (ValidationException e) {
            assertEquals("Email не может быть пустым", e.getMessage());
        }
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        User user = new User();
        user.setEmail("   ");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        try {
            userController.create(user);

        } catch (ValidationException e) {
            assertEquals("Email не может быть пустым", e.getMessage());
        }
    }


    @Test
    void shouldCreateUserWithValidEmail() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertNotNull(createdUser.getId());
    }

    // Тесты для логина
    @Test
    void shouldThrowExceptionWhenLoginIsNull() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin(null);
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        try {
            userController.create(user);

        } catch (ValidationException e) {
            assertEquals("Логин не может быть пустым", e.getMessage());
        }
    }

    @Test
    void shouldThrowExceptionWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("   ");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        try {
            userController.create(user);

        } catch (ValidationException e) {
            assertEquals("Логин не может быть пустым", e.getMessage());
        }
    }


    @Test
    void shouldCreateUserWithValidLogin() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("validlogin");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertNotNull(createdUser.getId());
    }

    // Тесты для имени
    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("testlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void shouldUseLoginWhenNameIsNull() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("testlogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void shouldUseLoginWhenNameIsBlank() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("testlogin");
        user.setName("   ");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void shouldKeepNameWhenNameIsProvided() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("testlogin");
        user.setName("Real Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertEquals("Real Name", createdUser.getName());
    }

    // Тесты для даты рождения
    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now().plusDays(1)); // Завтра

        try {
            userController.create(user);

        } catch (ValidationException e) {
            assertEquals("Дата рождения не может быть в будущем", e.getMessage());
        }
    }

    @Test
    void shouldCreateUserWithBirthdayToday() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now()); // Сегодня

        User createdUser = userController.create(user);
        assertNotNull(createdUser.getId());
    }

    @Test
    void shouldCreateUserWithPastBirthday() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1)); // В прошлом

        User createdUser = userController.create(user);
        assertNotNull(createdUser.getId());
    }
}