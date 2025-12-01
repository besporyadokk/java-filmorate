package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.LikesAddingException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;


    private static final String SQL_GET_ALL_FILMS =
            "SELECT f.*, m.name as mpa_name FROM films f LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id";
    private static final String SQL_GET_FILM_BY_ID =
            "SELECT f.*, m.name as mpa_name FROM films f LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?";
    private static final String SQL_ADD_FILM =
            "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_FILM =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
    private static final String SQL_DELETE_FILM = "DELETE FROM films WHERE id = ?";

    private static final String SQL_CHECK_LIKE = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String SQL_ADD_LIKE = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String SQL_REMOVE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String SQL_GET_POPULAR_FILMS =
            "SELECT f.*, m.name as mpa_name, COUNT(l.user_id) as likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "GROUP BY f.id " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";

    private static final String SQL_GET_GENRES_BY_FILM_ID =
            "SELECT g.id, g.name FROM film_genres fg " +
                    "JOIN genres g ON fg.genre_id = g.id " +
                    "WHERE fg.film_id = ? ORDER BY g.id";

    private static final String SQL_GET_ALL_GENRES_BY_FILM_IDS =
            "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                    "JOIN genres g ON fg.genre_id = g.id " +
                    "WHERE fg.film_id IN ";

    private static final String SQL_DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String SQL_ADD_FILM_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

    @Override
    public Map<Integer, Film> getFilms() {
        List<Film> films = jdbcTemplate.query(SQL_GET_ALL_FILMS, this::mapRowToFilm);

        if (!films.isEmpty()) {

            Map<Integer, List<Genre>> filmGenres = loadAllGenres(films);


            for (Film film : films) {
                film.setGenres(filmGenres.getOrDefault(film.getId(), new ArrayList<>()));
            }
        }

        Map<Integer, Film> filmMap = new HashMap<>();
        for (Film film : films) {
            filmMap.put(film.getId(), film);
        }
        return filmMap;
    }

    @Override
    public Film getFilm(Integer id) {
        List<Film> films = jdbcTemplate.query(SQL_GET_FILM_BY_ID, this::mapRowToFilm, id);
        if (films.isEmpty()) {
            return null;
        }
        Film film = films.get(0);
        loadGenres(film);
        return film;
    }

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(SQL_ADD_FILM, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        updateGenres(film);

        log.info("Добавлен новый фильм с ID: {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Integer id, Film film) {
        int updated = jdbcTemplate.update(SQL_UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                id);

        if (updated > 0) {
            updateGenres(film);
            log.info("Обновлен фильм с ID: {}", id);
            return getFilm(id);
        }
        return null;
    }

    @Override
    public void deleteFilm(Integer id) {
        jdbcTemplate.update(SQL_DELETE_FILM, id);
        log.info("Удален фильм с ID: {}", id);
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {

        Integer count = jdbcTemplate.queryForObject(SQL_CHECK_LIKE, Integer.class, filmId, userId);

        if (count != null && count > 0) {
            throw new LikesAddingException("Пользователь " + userId + " уже поставил лайк фильму " + filmId);
        }


        jdbcTemplate.update(SQL_ADD_LIKE, filmId, userId);

        log.debug("Добавлен лайк: фильм {}, пользователь {}", filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {

        Integer count = jdbcTemplate.queryForObject(SQL_CHECK_LIKE, Integer.class, filmId, userId);

        if (count == null || count == 0) {
            throw new LikesAddingException("Пользователь " + userId + " не ставил лайк фильму " + filmId);
        }


        int deleted = jdbcTemplate.update(SQL_REMOVE_LIKE, filmId, userId);

        if (deleted > 0) {
            log.debug("Удален лайк: фильм {}, пользователь {}", filmId, userId);
        }
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        int filmsCount = (count == null) ? 10 : count;


        Map<Integer, Film> filmMap = new HashMap<>();

        List<Film> films = jdbcTemplate.query(SQL_GET_POPULAR_FILMS, (rs, rowNum) -> {
            Film film = createFilmFromResultSet(rs);
            filmMap.put(film.getId(), film);
            return film;
        }, filmsCount);


        if (!films.isEmpty()) {
            loadGenresForFilms(filmMap);
        }

        return films;
    }

    private Film createFilmFromResultSet(ResultSet rs) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        java.sql.Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        film.setDuration(rs.getLong("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_rating_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        return film;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        return createFilmFromResultSet(rs);
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbcTemplate.query(SQL_GET_GENRES_BY_FILM_ID, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());

        Set<Genre> uniqueGenres = new LinkedHashSet<>(genres);
        film.setGenres(new ArrayList<>(uniqueGenres));
    }

    private Map<Integer, List<Genre>> loadAllGenres(List<Film> films) {
        if (films.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .toList();


        String inClause = "(" + String.join(",", Collections.nCopies(filmIds.size(), "?")) + ")";
        String sql = SQL_GET_ALL_GENRES_BY_FILM_IDS + inClause + " ORDER BY fg.film_id, g.id";

        Map<Integer, List<Genre>> filmGenres = new HashMap<>();

        jdbcTemplate.query(sql, filmIds.toArray(), rs -> {
            Integer filmId = rs.getInt("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));

            filmGenres.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        });


        for (List<Genre> genres : filmGenres.values()) {
            Set<Genre> uniqueGenres = new LinkedHashSet<>(genres);
            genres.clear();
            genres.addAll(uniqueGenres);
        }

        return filmGenres;
    }

    private void loadGenresForFilms(Map<Integer, Film> filmMap) {
        if (filmMap.isEmpty()) {
            return;
        }


        List<Integer> filmIds = new ArrayList<>(filmMap.keySet());


        String inClause = "(" + String.join(",", Collections.nCopies(filmIds.size(), "?")) + ")";
        String sql = SQL_GET_ALL_GENRES_BY_FILM_IDS + inClause + " ORDER BY fg.film_id, g.id";

        Map<Integer, List<Genre>> filmGenres = new HashMap<>();

        jdbcTemplate.query(sql, filmIds.toArray(), rs -> {
            Integer filmId = rs.getInt("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));

            filmGenres.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        });


        for (Map.Entry<Integer, Film> entry : filmMap.entrySet()) {
            Integer filmId = entry.getKey();
            Film film = entry.getValue();

            List<Genre> genres = filmGenres.get(filmId);
            if (genres != null && !genres.isEmpty()) {

                Set<Genre> uniqueGenres = new LinkedHashSet<>(genres);
                film.setGenres(new ArrayList<>(uniqueGenres));
            } else {
                film.setGenres(new ArrayList<>());
            }
        }
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update(SQL_DELETE_FILM_GENRES, film.getId());

        Set<Genre> uniqueGenres = new LinkedHashSet<>(film.getGenres());

        for (Genre genre : uniqueGenres) {
            jdbcTemplate.update(SQL_ADD_FILM_GENRE, film.getId(), genre.getId());
        }
    }
}