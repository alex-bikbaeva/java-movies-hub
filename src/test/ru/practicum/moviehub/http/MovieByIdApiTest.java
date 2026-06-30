package ru.practicum.moviehub.http;

import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MovieByIdApiTest extends MoviesApiTest {

    @Test
    void getMovieById_whenExists_returnsMovie() throws Exception {
        Movie created = createMovie("Бегущий по лезвию", 2017);

        HttpResponse<String> resp = get("/movies/" + created.getId());

        assertEquals(200, resp.statusCode());
        assertContentTypeJson(resp);

        Movie movie = GSON.fromJson(resp.body(), Movie.class);
        assertEquals(created.getId(), movie.getId());
        assertEquals(created.getTitle(), movie.getTitle());
    }

    @Test
    void getMovieById_whenNotFound_returns404() throws Exception {
        HttpResponse<String> resp = get("/movies/999");

        assertEquals(404, resp.statusCode());

        ErrorResponse error = GSON.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Фильм не найден", error.getError());
    }

    @Test
    void getMovieById_whenIdNotNumber_returns400() throws Exception {
        HttpResponse<String> resp = get("/movies/abc");

        assertEquals(400, resp.statusCode());

        ErrorResponse error = GSON.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Некорректный ID", error.getError());
    }
}