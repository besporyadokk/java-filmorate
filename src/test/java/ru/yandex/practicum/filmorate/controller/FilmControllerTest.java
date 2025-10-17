package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    // Тесты для названия фильма
    @Test
    void shouldThrowExceptionWhenFilmNameIsNull() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        try {
            filmController.create(film);

        } catch (ValidationException e) {
            assertEquals("Название не может быть пустым", e.getMessage());
        }
    }

    @Test
    void shouldThrowExceptionWhenFilmNameIsBlank() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        try {
            filmController.create(film);

        } catch (ValidationException e) {
            assertEquals("Название не может быть пустым", e.getMessage());
        }
    }

    @Test
    void shouldCreateFilmWithValidName() {
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm.getId());
    }

    // Тесты для описания фильма
    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("A".repeat(201)); // 201 символ
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        try {
            filmController.create(film);

        } catch (ValidationException e) {
            assertEquals("Максимальная длина описания - 200 символов", e.getMessage());
        }
    }

    @Test
    void shouldCreateFilmWithDescription200Characters() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("A".repeat(200)); // 200 символов
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm.getId());
    }

    @Test
    void shouldCreateFilmWithShortDescription() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Short");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm.getId());
    }

    // Тесты для даты релиза
    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeMinDate() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // На день раньше
        film.setDuration(120L);

        try {
            filmController.create(film);

        } catch (ValidationException e) {
            assertEquals("Дата релиза меньше минимальной", e.getMessage());
        }
    }

    @Test
    void shouldCreateFilmWithMinReleaseDate() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28)); // Минимальная дата
        film.setDuration(120L);

        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm.getId());
    }

    @Test
    void shouldCreateFilmWithLaterReleaseDate() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm.getId());
    }

    // Тесты для продолжительности
    @Test
    void shouldThrowExceptionWhenDurationNegative() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1L);

        try {
            filmController.create(film);

        } catch (ValidationException e) {
            assertEquals("Продолжительность фильма должна быть положительным числом", e.getMessage());
        }
    }

    @Test
    void shouldThrowExceptionWhenDurationZero() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0L);

        try {
            filmController.create(film);

        } catch (ValidationException e) {
            assertEquals("Продолжительность фильма должна быть положительным числом", e.getMessage());
        }
    }

    @Test
    void shouldCreateFilmWithPositiveDuration() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(1L);

        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm.getId());
    }
}