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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;


    private static final String SQL_GET_ALL_USERS = "SELECT * FROM users";
    private static final String SQL_GET_USER_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String SQL_ADD_USER = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String SQL_UPDATE_USER = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String SQL_DELETE_USER = "DELETE FROM users WHERE id = ?";

    private static final String SQL_GET_FRIENDS_BY_USER_ID = "SELECT friend_id, status FROM friends WHERE user_id = ?";
    private static final String SQL_GET_ALL_FRIENDS_BY_USER_IDS = "SELECT user_id, friend_id, status FROM friends WHERE user_id IN ";

    private static final String SQL_DELETE_FRIENDS = "DELETE FROM friends WHERE user_id = ?";
    private static final String SQL_ADD_FRIEND = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";

    @Override
    public Map<Integer, User> getUsers() {
        List<User> users = jdbcTemplate.query(SQL_GET_ALL_USERS, this::mapRowToUser);

        if (!users.isEmpty()) {

            Map<Integer, Map<Integer, FriendStatus>> userFriends = loadAllFriends(users);


            for (User user : users) {
                user.setFriends(userFriends.getOrDefault(user.getId(), new HashMap<>()));
            }
        }

        Map<Integer, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    @Override
    public User getUser(Integer id) {
        List<User> users = jdbcTemplate.query(SQL_GET_USER_BY_ID, this::mapRowToUser, id);
        if (users.isEmpty()) {
            return null;
        }
        User user = users.get(0);
        loadFriends(user);
        return user;
    }

    @Override
    public User addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(SQL_ADD_USER, new String[]{"id"});
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
        int updated = jdbcTemplate.update(SQL_UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                id);

        if (updated > 0) {
            updateFriends(user);
            log.info("Обновлен пользователь с ID: {}", id);
            return user;
        }
        return null;
    }

    @Override
    public void deleteUser(Integer id) {
        jdbcTemplate.update(SQL_DELETE_USER, id);
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
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_FRIENDS_BY_USER_ID, user.getId());

        Map<Integer, FriendStatus> friends = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer friendId = (Integer) row.get("friend_id");
            String status = (String) row.get("status");
            friends.put(friendId, createFriendStatus(friendId, status));
        }
        user.setFriends(friends);
    }

    private Map<Integer, Map<Integer, FriendStatus>> loadAllFriends(List<User> users) {
        if (users.isEmpty()) {
            return Collections.emptyMap();
        }


        List<Integer> userIds = users.stream()
                .map(User::getId)
                .toList();


        String inClause = "(" + String.join(",", Collections.nCopies(userIds.size(), "?")) + ")";
        String sql = SQL_GET_ALL_FRIENDS_BY_USER_IDS + inClause;

        Map<Integer, Map<Integer, FriendStatus>> allFriends = new HashMap<>();

        jdbcTemplate.query(sql, userIds.toArray(), rs -> {
            Integer userId = rs.getInt("user_id");
            Integer friendId = rs.getInt("friend_id");
            String status = rs.getString("status");

            allFriends.computeIfAbsent(userId, k -> new HashMap<>())
                    .put(friendId, createFriendStatus(friendId, status));
        });

        return allFriends;
    }

    private FriendStatus createFriendStatus(Integer friendId, String status) {
        try {
            return FriendStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.warn("Некорректный статус дружбы для пользователя {}: {}. Используется UNCONFIRMED",
                    friendId, status);
            return FriendStatus.UNCONFIRMED;
        }
    }

    private void updateFriends(User user) {

        jdbcTemplate.update(SQL_DELETE_FRIENDS, user.getId());


        for (Map.Entry<Integer, FriendStatus> entry : user.getFriends().entrySet()) {
            jdbcTemplate.update(SQL_ADD_FRIEND, user.getId(), entry.getKey(), entry.getValue().toString());
        }
    }
}