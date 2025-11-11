package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {

    @Autowired
    private FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.getFilms();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return filmService.addFilm(film);
    }


    @PutMapping
    public Film update(@RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film sendLike(
            @PathVariable Integer id,
            @PathVariable Integer userId
    ) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(
            @PathVariable Integer id,
            @PathVariable Integer userId
    ) {
        return filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") Integer count
    ) {
        return filmService.getPopularFilms(count);
    }
}
