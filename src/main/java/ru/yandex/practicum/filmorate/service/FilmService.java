package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public Collection<Film> getFilms() {
        return new ArrayList<>(filmStorage.getFilms().values());
    }

    public Film addFilm(Film film) {
        log.info("Обрабатывается запрос добавления фильма");
        validateFilm(film);
        validateMpa(film.getMpa().getId());
        validateGenres(film.getGenres());

        filmStorage.addFilm(film);
        log.info("Фильм успешно добавлен");
        return film;
    }

    public Film updateFilm(Film film) {
        log.info("Обрабатывается запрос обновления фильма");
        if (!filmStorage.getFilms().containsKey(film.getId())) {
            log.warn("!! Попытка обновления несуществующего фильма с id: {}", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        validateFilm(film);
        validateMpa(film.getMpa().getId());
        validateGenres(film.getGenres());

        filmStorage.updateFilm(film.getId(), film);
        log.info("Фильм успешно обновлён");
        return film;
    }

    public Film getFilm(Integer id) {
        Film film = filmStorage.getFilm(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
    }

    public Film addLike(Integer filmId, Integer userId) {

        Film film = getFilm(filmId);
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }


        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return film;
    }

    public Film removeLike(Integer filmId, Integer userId) {

        Film film = getFilm(filmId);
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }


        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
        return film;
    }

    public List<Film> getPopularFilms(Integer count) {
        int filmsCount = (count == null) ? 10 : count;


        return filmStorage.getPopularFilms(filmsCount);
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getAllMpa();
    }

    public Mpa getMpaById(Integer id) {
        Mpa mpa = mpaStorage.getMpaById(id);
        if (mpa == null) {
            throw new NotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
        return mpa;
    }

    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(Integer id) {
        Genre genre = genreStorage.getGenreById(id);
        if (genre == null) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        return genre;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("!! Фильм с пустым названием");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("!! Фильм с описанием длиннее 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("!! Фильм с неверной датой релиза");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("!! Фильм с неверной продолжительностью");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }

    private void validateMpa(Integer mpaId) {
        if (mpaStorage.getMpaById(mpaId) == null) {
            throw new NotFoundException("Рейтинг MPA с id " + mpaId + " не найден");
        }
    }

    private void validateGenres(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }


        List<Integer> genreIds = new ArrayList<>();
        for (Genre genre : genres) {
            if (genre.getId() == null) {
                throw new ValidationException("ID жанра не может быть null");
            }
            genreIds.add(genre.getId());
        }


        if (!genreStorage.checkGenresExist(genreIds)) {
            throw new NotFoundException("Один или несколько жанров не найдены");
        }
    }
}