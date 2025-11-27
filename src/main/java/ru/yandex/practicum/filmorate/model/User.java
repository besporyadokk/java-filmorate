package ru.yandex.practicum.filmorate.model;


import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Integer id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Integer> friends;
    private FriendStatus friendStatus;

    public User() {
        friends = new HashSet<>();
    }

    public boolean addFriend(Integer id) {
        return friends.add(id);
    }

    public boolean deleteFriend(Integer id) {
        return friends.remove(id);
    }
}
