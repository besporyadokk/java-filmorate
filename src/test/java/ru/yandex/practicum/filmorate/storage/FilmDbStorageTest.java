package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class})  // Импортируем оба storage
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserStorage userStorage;  // Добавляем UserStorage
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы в правильном порядке (с учетом foreign keys)
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        // Сбрасываем автоинкремент
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");

        // Инициализируем справочные данные (MPA и жанры)
        jdbcTemplate.update("MERGE INTO mpa_ratings (id, name) KEY (id) VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17')");
        jdbcTemplate.update("MERGE INTO genres (id, name) KEY (id) VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм'), (4, 'Триллер'), (5, 'Документальный'), (6, 'Боевик')");
    }

    @Test
    void testAddAndRemoveLike() {
        // Сначала создаем пользователя для лайков через UserStorage
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.addUser(user);
        Integer userId = savedUser.getId();

        Film film = createTestFilm();
        Film savedFilm = filmStorage.addFilm(film);

        // Добавляем лайк через новый метод
        filmStorage.addLike(savedFilm.getId(), userId);

        // Проверяем, что лайк добавился в БД
        Integer likeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class, savedFilm.getId(), userId);
        assertThat(likeCount).isEqualTo(1);

        // Удаляем лайк через новый метод
        filmStorage.removeLike(savedFilm.getId(), userId);

        // Проверяем, что лайк удалился из БД
        likeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class, savedFilm.getId(), userId);
        assertThat(likeCount).isEqualTo(0);
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
    void testGetPopularFilms() {
        // Создаем двух пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser2 = userStorage.addUser(user2);

        // Создаем три фильма
        Film film1 = createTestFilm();
        film1.setName("Film One");
        Film savedFilm1 = filmStorage.addFilm(film1);

        Film film2 = createTestFilm();
        film2.setName("Film Two");
        Film savedFilm2 = filmStorage.addFilm(film2);

        Film film3 = createTestFilm();
        film3.setName("Film Three");
        Film savedFilm3 = filmStorage.addFilm(film3);

        // Добавляем лайки:
        // Film1 - 2 лайка
        // Film2 - 1 лайк
        // Film3 - 0 лайков
        filmStorage.addLike(savedFilm1.getId(), savedUser1.getId());
        filmStorage.addLike(savedFilm1.getId(), savedUser2.getId());
        filmStorage.addLike(savedFilm2.getId(), savedUser1.getId());

        // Получаем популярные фильмы (2 самых популярных)
        List<Film> popularFilms = filmStorage.getPopularFilms(2);

        // Проверяем, что вернулось 2 фильма
        assertThat(popularFilms).hasSize(2);

        // Проверяем порядок: Film1 должен быть первым (2 лайка), Film2 - вторым (1 лайк)
        assertThat(popularFilms.get(0).getName()).isEqualTo("Film One");
        assertThat(popularFilms.get(1).getName()).isEqualTo("Film Two");
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