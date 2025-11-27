package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private UserController userController;

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
        assertEquals("Email не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        User user = new User();
        user.setEmail("   ");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
        assertEquals("Email не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldCreateUserWithValidEmail() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertNotNull(createdUser.getId());
    }

    @Test
    void shouldThrowExceptionWhenLoginIsNull() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin(null);
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin("   ");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldCreateUserWithValidLogin() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin("validlogin");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertNotNull(createdUser.getId());
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin("testlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void shouldUseLoginWhenNameIsNull() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin("testlogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void shouldUseLoginWhenNameIsBlank() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin("testlogin");
        user.setName("   ");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void shouldKeepNameWhenNameIsProvided() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin("testlogin");
        user.setName("Real Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);
        assertEquals("Real Name", createdUser.getName());
    }

    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now().plusDays(1)); // Завтра

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void shouldCreateUserWithBirthdayToday() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now()); // Сегодня

        User createdUser = userController.create(user);
        assertNotNull(createdUser.getId());
    }

    @Test
    void shouldCreateUserWithPastBirthday() {
        User user = new User();
        user.setEmail("unique" + System.currentTimeMillis() + "@email.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1)); // В прошлом

        User createdUser = userController.create(user);
        assertNotNull(createdUser.getId());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        User user = new User();
        user.setEmail("mail.ru"); // Некорректный email без @
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
        assertEquals("Email должен быть корректным адресом", exception.getMessage());
    }
}