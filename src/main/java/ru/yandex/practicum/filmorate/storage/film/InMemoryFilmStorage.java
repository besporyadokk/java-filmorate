package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.Map;


@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films;

    public InMemoryFilmStorage() {
        films = new HashMap<>();
    }

    @Override
    public void addFilm(Film film) {
        films.put(film.getId(), film);
    }

    @Override
    public Film getFilm(Integer id) {
        return films.get(id);
    }

    @Override
    public Map<Integer, Film> getFilms() {
        return films;
    }

    @Override
    public void updateFilm(Integer id, Film film) {
        Film filmToUpdate = films.get(id);
        if (filmToUpdate != null) {
            filmToUpdate.setName(film.getName());
            filmToUpdate.setDescription(film.getDescription());
            filmToUpdate.setReleaseDate(film.getReleaseDate());
            filmToUpdate.setDuration(film.getDuration());
            filmToUpdate.setLikes(film.getLikes());
        }
    }

}
