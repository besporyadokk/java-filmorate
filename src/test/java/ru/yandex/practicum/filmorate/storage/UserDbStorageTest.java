package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testFindUserById() {
        // Сначала создаем пользователя
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.addUser(user);

        // Теперь ищем его по ID
        User foundUser = userStorage.getUser(savedUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getLogin()).isEqualTo("testlogin");
    }

    @Test
    void testGetAllUsers() {
        // Создаем двух пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        userStorage.addUser(user1);
        userStorage.addUser(user2);

        Map<Integer, User> users = userStorage.getUsers();

        assertThat(users).hasSize(2);
        assertThat(users.values()).extracting(User::getEmail)
                .contains("user1@example.com", "user2@example.com");
    }

    @Test
    void testUpdateUser() {
        // Создаем пользователя
        User user = new User();
        user.setEmail("old@example.com");
        user.setLogin("oldlogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.addUser(user);

        // Обновляем данные
        User updatedUser = new User();
        updatedUser.setId(savedUser.getId());
        updatedUser.setEmail("new@example.com");
        updatedUser.setLogin("newlogin");
        updatedUser.setName("New Name");
        updatedUser.setBirthday(LocalDate.of(1995, 5, 5));

        User result = userStorage.updateUser(savedUser.getId(), updatedUser);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getLogin()).isEqualTo("newlogin");
        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void testDeleteUser() {
        // Создаем пользователя
        User user = new User();
        user.setEmail("delete@example.com");
        user.setLogin("deletelogin");
        user.setName("Delete User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.addUser(user);

        // Удаляем пользователя
        userStorage.deleteUser(savedUser.getId());

        User deletedUser = userStorage.getUser(savedUser.getId());

        assertThat(deletedUser).isNull();
    }

    @Test
    void testAddFriend() {
        // Создаем двух пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);

        // Добавляем друга
        savedUser1.addFriend(savedUser2.getId(), FriendStatus.UNCONFIRMED);
        userStorage.updateUser(savedUser1.getId(), savedUser1);

        // Проверяем, что друг добавлен
        User userWithFriend = userStorage.getUser(savedUser1.getId());

        assertThat(userWithFriend.getFriends()).hasSize(1);
        assertThat(userWithFriend.getFriends()).containsKey(savedUser2.getId());
        assertThat(userWithFriend.getFriends().get(savedUser2.getId())).isEqualTo(FriendStatus.UNCONFIRMED);
    }

    @Test
    void testGetNonExistentUser() {
        User user = userStorage.getUser(999);
        assertThat(user).isNull();
    }
}