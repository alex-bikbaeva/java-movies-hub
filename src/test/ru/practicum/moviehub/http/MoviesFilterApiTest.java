package ru.practicum.moviehub.http;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesFilterApiTest extends MoviesApiTest {

    @Test
    void getMoviesByYear_whenExists_returnsFilteredMovies() throws Exception {
        createMovie("Дюна", 2021);
        createMovie("Прибытие", 2016);
        createMovie("Дюна.Часть 2", 2024);

        HttpResponse<String> resp = get("/movies?year=2021");

        assertEquals(200, resp.statusCode());

        Type listType = new TypeToken<List<Movie>>() {}.getType();
        List<Movie> movies = GSON.fromJson(resp.body(), listType);

        assertEquals(1, movies.size());
        assertEquals("Дюна", movies.get(0).getTitle());
    }

    @Test
    void getMoviesByYear_whenNoMatches_returnsEmptyArray() throws Exception {
        createMovie("Прибытие", 2016);

        HttpResponse<String> resp = get("/movies?year=2023");

        assertEquals(200, resp.statusCode());
        assertEquals("[]", resp.body().trim());
    }

    @Test
    void getMoviesByYear_whenYearInvalid_returns400() throws Exception {
        HttpResponse<String> resp = get("/movies?year=abc");

        assertEquals(400, resp.statusCode());

        ErrorResponse error = GSON.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Некорректный параметр запроса — 'year'", error.getError());
    }
}