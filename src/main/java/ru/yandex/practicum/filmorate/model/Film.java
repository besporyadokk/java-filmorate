package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;

    private Mpa mpa;
    private List<Genre> genres;

    public Film() {

        genres = new ArrayList<>();
    }

}