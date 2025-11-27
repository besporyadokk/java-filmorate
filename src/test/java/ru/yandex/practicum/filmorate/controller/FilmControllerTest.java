package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Test
    void shouldThrowExceptionWhenFilmNameIsNull() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFilmNameIsBlank() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
        assertEquals("Название не может быть пустым", exception.getMessage());
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

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("A".repeat(201)); // 201 символ
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
        assertEquals("Максимальная длина описания - 200 символов", exception.getMessage());
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

    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeMinDate() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // На день раньше
        film.setDuration(120L);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
        assertEquals("Дата релиза меньше минимальной", exception.getMessage());
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

    @Test
    void shouldThrowExceptionWhenDurationNegative() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1L);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDurationZero() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0L);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
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