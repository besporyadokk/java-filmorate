package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;


    private static final String SQL_GET_ALL_GENRES = "SELECT * FROM genres ORDER BY id";
    private static final String SQL_GET_GENRE_BY_ID = "SELECT * FROM genres WHERE id = ?";
    private static final String SQL_CHECK_GENRES_EXIST = "SELECT COUNT(*) FROM genres WHERE id IN ";

    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query(SQL_GET_ALL_GENRES, (rs, rowNum) -> mapRowToGenre(rs));
    }

    @Override
    public Genre getGenreById(Integer id) {
        List<Genre> genreList = jdbcTemplate.query(SQL_GET_GENRE_BY_ID,
                (rs, rowNum) -> mapRowToGenre(rs), id);
        return genreList.isEmpty() ? null : genreList.get(0);
    }

    @Override
    public boolean checkGenresExist(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return true;
        }


        Set<Integer> uniqueIds = new HashSet<>(genreIds);


        String inClause = "(" + String.join(",", Collections.nCopies(uniqueIds.size(), "?")) + ")";
        String sql = SQL_CHECK_GENRES_EXIST + inClause;


        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, uniqueIds.toArray());


        return count != null && count == uniqueIds.size();
    }

    private Genre mapRowToGenre(ResultSet rs) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}