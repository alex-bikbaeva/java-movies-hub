package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MoviesStore {
    private final Map<Long, Movie> movies = new LinkedHashMap<>();
    private long nextId = 1;

    public List<Movie> findAll() {
        return new ArrayList<>(movies.values());
    }

    public List<Movie> findByYear(int year) {
        return movies.values().stream()
                .filter(movie -> movie.getYear() == year)
                .toList();
    }

    public Movie findById(long id) {
        return movies.get(id);
    }

    public Movie save(Movie movie) {
        Movie stored = new Movie(nextId++, movie.getTitle(), movie.getYear());
        movies.put(stored.getId(), stored);
        return stored;
    }

    public boolean deleteById(long id) {
        return movies.remove(id) != null;
    }

    public void clear() {
        movies.clear();
        nextId = 1;
    }
}