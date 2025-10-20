package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    Map<Integer, Film> filmsMap = new HashMap<>();
    private int nextId = 1;


    @GetMapping
    public Collection<Film> findAll() {
        return filmsMap.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Обрабатывается запрос на добавление фильма");
        validateFilm(film);

        film.setId(getNextId());
        filmsMap.put(film.getId(), film);
        log.info("Фильм успешно добавлен");
        return film;
    }


    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Обрабатывается запрос на обновление фильма");
        if (!filmsMap.containsKey(film.getId())) {
            log.warn("!! Попытка обновления несуществующего фильма с id: {}", film.getId());
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }
        validateFilm(film);

        filmsMap.put(film.getId(), film);
        log.info("Фильм обновлён");
        return film;
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

    // вспомогательный метод для генерации идентификатора нового поста
    private Integer getNextId() {
        return nextId++;
    }
}
