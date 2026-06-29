package ru.practicum.moviehub.http;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesGetApiTest extends MoviesApiTest {

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpResponse<String> resp = get("/movies");

        assertEquals(200, resp.statusCode());
        assertContentTypeJson(resp);
        assertEquals("[]", resp.body().trim());
    }

    @Test
    void getMovies_whenHasData_returnsAddedMovies() throws Exception {
        Movie first = createMovie("Интерстеллар", 2014);
        Movie second = createMovie("Дьявол носит Prada 2", 2026);

        HttpResponse<String> resp = get("/movies");

        assertEquals(200, resp.statusCode());
        assertContentTypeJson(resp);

        Type listType = new ListOfMoviesTypeToken().getType();
        List<Movie> movies = GSON.fromJson(resp.body(), listType);

        assertEquals(2, movies.size());
        assertEquals(first.getTitle(), movies.get(0).getTitle());
        assertEquals(second.getTitle(), movies.get(1).getTitle());
    }

    @Test
    void unsupportedMethod_returns405() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode());
    }
}