package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.*;

@Data
public class User {
    private Integer id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Map<Integer, FriendStatus> friends;

    public User() {
        friends = new HashMap<>();
    }

    public boolean addFriend(Integer id, FriendStatus status) {
        return friends.put(id, status) == null;
    }

    public boolean deleteFriend(Integer id) {
        return friends.remove(id) != null;
    }

    public FriendStatus getFriendStatus(Integer friendId) {
        return friends.get(friendId);
    }

    public Set<Integer> getFriendIds() {
        return friends.keySet();
    }
}