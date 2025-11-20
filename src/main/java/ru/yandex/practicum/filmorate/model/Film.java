package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private Set<Integer> likes;
    private MpaRating mpaRating;
    private Genre genre;


    public Film() {
        likes = new HashSet<>();
    }

    public boolean addLike(Integer id) {
        return likes.add(id);
    }

    public boolean removeLike(Integer id) {
        return likes.remove(id);
    }
}
