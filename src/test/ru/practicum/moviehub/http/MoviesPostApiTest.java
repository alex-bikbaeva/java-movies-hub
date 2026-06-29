package ru.practicum.moviehub.http;

import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesPostApiTest extends MoviesApiTest {

    @Test
    void postMovies_whenValid_returnsCreatedMovie() throws Exception {
        HttpResponse<String> resp = postJson("/movies", """
                {"title":"Прибытие","year":2016}
                """);

        assertEquals(201, resp.statusCode());
        assertContentTypeJson(resp);

        Movie movie = GSON.fromJson(resp.body(), Movie.class);
        assertTrue(movie.getId() > 0);
        assertEquals("Прибытие", movie.getTitle());
        assertEquals(2016, movie.getYear());
    }

    @Test
    void postMovies_whenTitleEmpty_returns422() throws Exception {
        HttpResponse<String> resp = postJson("/movies", """
                {"title":"   ","year":2016}
                """);

        assertEquals(422, resp.statusCode());

        ErrorResponse error = GSON.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Ошибка валидации", error.getError());
        assertTrue(error.getDetails().contains("Название не должно быть пустым"));
    }

    @Test
    void postMovies_whenTitleTooLong_returns422() throws Exception {
        String longTitle = "x".repeat(101);

        HttpResponse<String> resp = postJson("/movies",
                "{\"title\":\"" + longTitle + "\",\"year\":2016}");

        assertEquals(422, resp.statusCode());

        ErrorResponse error = GSON.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(error.getDetails().contains("Название не должно быть длиннее 100 символов"));
    }

    @Test
    void postMovies_whenYearInvalid_returns422() throws Exception {
        HttpResponse<String> resp = postJson("/movies", """
                {"title":"Старый фильм","year":1800}
                """);

        assertEquals(422, resp.statusCode());

        ErrorResponse error = GSON.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Ошибка валидации", error.getError());
        assertFalse(error.getDetails().isEmpty());
    }

    @Test
    void postMovies_whenContentTypeInvalid_returns415() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"title":"Прибытие","year":2016}
                        """))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(415, resp.statusCode());
    }

    @Test
    void postMovies_whenJsonInvalid_returns400() throws Exception {
        HttpResponse<String> resp = postJson("/movies", """
                {"title":"Сломленный","year":}
                """);

        assertEquals(400, resp.statusCode());

        ErrorResponse error = GSON.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Некорректный JSON", error.getError());
    }
}