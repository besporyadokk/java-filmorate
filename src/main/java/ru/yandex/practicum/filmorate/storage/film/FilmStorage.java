package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {
    Map<Integer, Film> getFilms();

    Film getFilm(Integer id);

    Film addFilm(Film film);

    Film updateFilm(Integer id, Film film);

    void deleteFilm(Integer id);


    void addLike(Integer filmId, Integer userId);

    void removeLike(Integer filmId, Integer userId);

    List<Film> getPopularFilms(Integer count);
}