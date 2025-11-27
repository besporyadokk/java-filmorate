package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.*;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<Integer, User> getUsers() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser);
        Map<Integer, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
            loadFriends(user);
        }
        return userMap;
    }

    @Override
    public User getUser(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        if (users.isEmpty()) {
            return null;
        }
        User user = users.get(0);
        loadFriends(user);
        return user;
    }

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        log.info("Добавлен новый пользователь с ID: {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(Integer id, User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                id);

        if (updated > 0) {
            // Обновляем друзей
            updateFriends(user);
            log.info("Обновлен пользователь с ID: {}", id);
            return user;
        }
        return null;
    }

    @Override
    public void deleteUser(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
        log.info("Удален пользователь с ID: {}", id);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));

        java.sql.Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            user.setBirthday(birthday.toLocalDate());
        }

        return user;
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id, status FROM friends WHERE user_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, user.getId());

        Map<Integer, FriendStatus> friends = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer friendId = (Integer) row.get("friend_id");
            String status = (String) row.get("status");
            friends.put(friendId, FriendStatus.valueOf(status));
        }
        user.setFriends(friends);
    }

    private void updateFriends(User user) {

        String deleteSql = "DELETE FROM friends WHERE user_id = ?";
        jdbcTemplate.update(deleteSql, user.getId());


        String insertSql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        for (Map.Entry<Integer, FriendStatus> entry : user.getFriends().entrySet()) {
            jdbcTemplate.update(insertSql, user.getId(), entry.getKey(), entry.getValue().toString());
        }
    }
}