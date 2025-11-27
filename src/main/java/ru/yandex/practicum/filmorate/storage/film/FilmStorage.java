package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;

public interface FilmStorage {
    Map<Integer, Film> getFilms();

    Film getFilm(Integer id);

    Film addFilm(Film film);

    Film updateFilm(Integer id, Film film);

    void deleteFilm(Integer id);
}