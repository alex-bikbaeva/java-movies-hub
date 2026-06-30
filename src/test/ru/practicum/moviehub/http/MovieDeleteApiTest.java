package ru.practicum.moviehub.http;

import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MovieDeleteApiTest extends MoviesApiTest {

    @Test
    void deleteMovie_whenExists_returns204() throws Exception {
        Movie created = createMovie("Пришелец", 1979);

        HttpResponse<String> resp = delete("/movies/" + created.getId());

        assertEquals(204, resp.statusCode());

        HttpResponse<String> checkResp = get("/movies/" + created.getId());
        assertEquals(404, checkResp.statusCode());
    }

    @Test
    void deleteMovie_whenNotFound_returns404() throws Exception {
        HttpResponse<String> resp = delete("/movies/999");

        assertEquals(404, resp.statusCode());

        ErrorResponse error = GSON.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Фильм не найден", error.getError());
    }

    @Test
    void deleteMovie_whenIdNotNumber_returns400() throws Exception {
        HttpResponse<String> resp = delete("/movies/abc");

        assertEquals(400, resp.statusCode());

        ErrorResponse error = GSON.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Некорректный ID", error.getError());
    }
}