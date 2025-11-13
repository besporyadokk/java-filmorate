package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.LikesAddingException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private int nextId = 1;

    @Autowired
    private UserStorage userStorage;
    @Autowired
    private FilmStorage filmStorage;


    public Collection<Film> getFilms() {
        return new ArrayList<>(filmStorage.getFilms().values());
    }

    public Film addFilm(Film film) {
        log.info("Обрабатывается запрос на добавление фильма");
        validateFilm(film);

        film.setId(getNextId());
        filmStorage.addFilm(film);
        log.info("Фильм успешно добавлен");
        return film;
    }

    public Film updateFilm(@RequestBody Film film) {
        log.info("Обрабатывается запрос на обновление фильма");
        if (!filmStorage.getFilms().containsKey(film.getId())) {
            log.warn("!! Попытка обновления несуществующего фильма с id: {}", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        validateFilm(film);

        filmStorage.updateFilm(film.getId(), film);
        log.info("Фильм обновлён");
        return film;
    }


    public Film addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilm(filmId);
        User user = userStorage.getUser(userId);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (film.getLikes().contains(userId)) {
            throw new LikesAddingException("Пользователи с айди " + userId + " уже добавил лайк фильму с айди  "
                    + filmId);
        }

        film.addLike(userId);

        log.info("Пользователь {} поставил лайк фильму {} ", userId, filmId);

        return film;
    }


    public Film removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilm(filmId);
        User user = userStorage.getUser(userId);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (!film.getLikes().contains(userId)) {
            throw new LikesAddingException("Пользователи с айди " + userId + " не добавил лайк фильму с айди  "
                    + film);
        }

        film.removeLike(userId);

        log.info("Пользователь {} удалил лайк фильму {} ", userId, filmId);

        return film;
    }

    public List<Film> getPopularFilms(Integer count) {
        return filmStorage.getFilms().values().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("!! Фильм с пустым названием");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("!! Фильм с описанием длиной больше максимальной");
            throw new ValidationException("Максимальная длина описания - " + MAX_DESCRIPTION_LENGTH + " символов");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("!! Фильм с датой релиза меньше минимальной");
            throw new ValidationException("Дата релиза меньше минимальной");
        }
        if (film.getDuration() <= 0) {
            log.warn("!! Фильм с отрицательной продолжительностью");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private Integer getNextId() {
        return nextId++;
    }
}
