package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(FilmDbStorage.class)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Убедимся, что данные инициализированы
        // Spring Boot автоматически выполнит schema.sql и data.sql
    }

    @Test
    void testAddFilm() {
        Film film = createTestFilm();

        Film savedFilm = filmStorage.addFilm(film);

        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getId()).isNotNull();
        assertThat(savedFilm.getName()).isEqualTo("Test Film");
        assertThat(savedFilm.getDescription()).isEqualTo("Test Description");
        assertThat(savedFilm.getMpa().getId()).isEqualTo(1);
    }

    @Test
    void testGetFilmById() {
        Film film = createTestFilm();
        Film savedFilm = filmStorage.addFilm(film);

        Film foundFilm = filmStorage.getFilm(savedFilm.getId());

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getId()).isEqualTo(savedFilm.getId());
        assertThat(foundFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void testGetAllFilms() {
        Film film1 = createTestFilm();
        film1.setName("Film One");

        Film film2 = createTestFilm();
        film2.setName("Film Two");

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);

        Map<Integer, Film> films = filmStorage.getFilms();

        assertThat(films).hasSize(2);
        assertThat(films.values()).extracting(Film::getName)
                .contains("Film One", "Film Two");
    }

    @Test
    void testUpdateFilm() {
        Film film = createTestFilm();
        Film savedFilm = filmStorage.addFilm(film);

        Film updatedFilm = createTestFilm();
        updatedFilm.setId(savedFilm.getId());
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");

        Film result = filmStorage.updateFilm(savedFilm.getId(), updatedFilm);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Film");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void testDeleteFilm() {
        Film film = createTestFilm();
        Film savedFilm = filmStorage.addFilm(film);

        filmStorage.deleteFilm(savedFilm.getId());

        Film deletedFilm = filmStorage.getFilm(savedFilm.getId());
        assertThat(deletedFilm).isNull();
    }

    @Test
    void testFilmWithGenres() {
        Film film = createTestFilm();

        // Добавляем жанры
        List<Genre> genres = new ArrayList<>();
        Genre genre1 = new Genre();
        genre1.setId(1);
        genre1.setName("Комедия");
        genres.add(genre1);

        Genre genre2 = new Genre();
        genre2.setId(2);
        genre2.setName("Драма");
        genres.add(genre2);

        film.setGenres(genres);

        Film savedFilm = filmStorage.addFilm(film);

        Film foundFilm = filmStorage.getFilm(savedFilm.getId());

        assertThat(foundFilm.getGenres()).hasSize(2);
        assertThat(foundFilm.getGenres()).extracting(Genre::getId)
                .contains(1, 2);
    }

    @Test
    void testFilmWithLikes() {
        // Сначала создаем пользователя для лайков
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user@example.com", "userlogin", "User Name", LocalDate.of(1990, 1, 1));

        Film film = createTestFilm();
        Film savedFilm = filmStorage.addFilm(film);

        // Добавляем лайки
        savedFilm.addLike(1); // ID пользователя = 1
        filmStorage.updateFilm(savedFilm.getId(), savedFilm);

        Film foundFilm = filmStorage.getFilm(savedFilm.getId());

        assertThat(foundFilm.getLikes()).hasSize(1);
        assertThat(foundFilm.getLikes()).contains(1);
    }

    @Test
    void testGetNonExistentFilm() {
        Film film = filmStorage.getFilm(999);
        assertThat(film).isNull();
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120L);

        Mpa mpa = new Mpa();
        mpa.setId(1); // Должен соответствовать данным из data.sql
        mpa.setName("G");
        film.setMpa(mpa);

        return film;
    }
}