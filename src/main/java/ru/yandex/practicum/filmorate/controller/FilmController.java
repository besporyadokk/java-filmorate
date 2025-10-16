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

    Map<Integer, Film> filmsMap = new HashMap<>();
    private int nextId = 1;


    @GetMapping
    public Collection<Film> findAll() {
        return filmsMap.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Обрабатывается запрос на добавление фильма");
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("!! Добавление фильма с пустым названием");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("!! Добавление фильма с описанием длиной больше масимальной");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("!! Добавление фильма с датой релиза меньше минимальной");
            throw new ValidationException("Дата релиза меньше минимальной");
        }
        if (film.getDuration() < 0) {
            log.warn("!! Добавление фильма с отрицательной продолжительностью");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }


        film.setId(getNextId());
        filmsMap.put(film.getId(), film);
        log.info("Фильм успешно добавлен");
        return film;
    }


    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Обрабатывается запрос на обновление фильма");
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("!! Обновление фильма с пустым названием");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("!! Обновление фильма с описанием длиной больше масимальной");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("!! Обновление фильма с датой релиза меньше минимальной");
            throw new ValidationException("Дата релиза меньше минимальной");
        }
        if (film.getDuration() < 0) {
            log.warn("!! Обновление фильма с отрицательной продолжительностью");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        filmsMap.put(film.getId(), film);
        log.info("Фильм обновлён");
        return film;
    }


    // вспомогательный метод для генерации идентификатора нового поста
    private Integer getNextId() {
        Integer currentMaxId = (int) filmsMap.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
