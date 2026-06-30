package ru.practicum.moviehub.http;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoviesHandler extends BaseHttpHandler {
    private static final String BASE_PATH = "/movies";
    private static final int MIN_YEAR = 1888;

    private static final String ERROR_VALIDATION = "Ошибка валидации";
    private static final String ERROR_INVALID_JSON = "Некорректный JSON";
    private static final String ERROR_UNSUPPORTED_CONTENT_TYPE = "Неподдерживаемый Content-Type";
    private static final String ERROR_METHOD_NOT_ALLOWED = "Метод не поддерживается";
    private static final String ERROR_MOVIE_NOT_FOUND = "Фильм не найден";
    private static final String ERROR_INVALID_ID = "Некорректный ID";
    private static final String ERROR_INVALID_YEAR_PARAM = "Некорректный параметр запроса — 'year'";
    private static final String ERROR_ENDPOINT_NOT_FOUND = "Эндпоинт не найден";

    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = normalizePath(ex.getRequestURI().getPath());

        if (path.equals(BASE_PATH) || path.equals(BASE_PATH + "/")) {
            handleCollectionRequest(ex, method);
            return;
        }

        if (path.startsWith(BASE_PATH + "/")) {
            String idPart = path.substring((BASE_PATH + "/").length());
            handleItemRequest(ex, method, idPart);
            return;
        }

        sendError(ex, 404, ERROR_ENDPOINT_NOT_FOUND);
    }

    private String normalizePath(String path) {
        if (path != null && path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private void handleCollectionRequest(HttpExchange ex, String method) throws IOException {
        if (method.equalsIgnoreCase("GET")) {
            handleGetMovies(ex);
            return;
        }

        if (method.equalsIgnoreCase("POST")) {
            handleCreateMovie(ex);
            return;
        }

        sendError(ex, 405, ERROR_METHOD_NOT_ALLOWED);
    }

    private void handleItemRequest(HttpExchange ex, String method, String idPart) throws IOException {
        if (method.equalsIgnoreCase("GET")) {
            handleGetMovieById(ex, idPart);
            return;
        }

        if (method.equalsIgnoreCase("DELETE")) {
            handleDeleteMovie(ex, idPart);
            return;
        }

        sendError(ex, 405, ERROR_METHOD_NOT_ALLOWED);
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();

        if (query == null || query.isBlank()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                params.put(parts[0], parts[1]);
            }
        }

        return params;
    }

    private void handleGetMovies(HttpExchange ex) throws IOException {
        Map<String, String> params = parseQueryParams(ex.getRequestURI().getQuery());

        if (params.isEmpty()) {
            sendJson(ex, 200, GSON.toJson(store.findAll()));
            return;
        }

        if (!params.containsKey("year")) {
            sendError(ex, 400, ERROR_INVALID_YEAR_PARAM);
            return;
        }

        int year;
        try {
            year = Integer.parseInt(params.get("year"));
        } catch (NumberFormatException e) {
            sendError(ex, 400, ERROR_INVALID_YEAR_PARAM);
            return;
        }

        sendJson(ex, 200, GSON.toJson(store.findByYear(year)));
    }

    private void handleCreateMovie(HttpExchange ex) throws IOException {
        if (!hasJsonContentType(ex)) {
            sendError(ex, 415, ERROR_UNSUPPORTED_CONTENT_TYPE);
            return;
        }

        Movie movie;
        try {
            movie = parseJsonBody(ex, Movie.class);
        } catch (JsonSyntaxException e) {
            sendError(ex, 400, ERROR_INVALID_JSON);
            return;
        }

        List<String> details = validateMovie(movie);
        if (!details.isEmpty()) {
            sendValidationError(ex, details);
            return;
        }

        Movie created = store.save(movie);
        sendJson(ex, 201, GSON.toJson(created));
    }

    private void handleGetMovieById(HttpExchange ex, String idPart) throws IOException {
        Long id = parseId(idPart);
        if (id == null) {
            sendError(ex, 400, ERROR_INVALID_ID);
            return;
        }

        Movie movie = store.findById(id);
        if (movie == null) {
            sendError(ex, 404, ERROR_MOVIE_NOT_FOUND);
            return;
        }

        sendJson(ex, 200, GSON.toJson(movie));
    }

    private void handleDeleteMovie(HttpExchange ex, String idPart) throws IOException {
        Long id = parseId(idPart);
        if (id == null) {
            sendError(ex, 400, ERROR_INVALID_ID);
            return;
        }

        boolean deleted = store.deleteById(id);
        if (!deleted) {
            sendError(ex, 404, ERROR_MOVIE_NOT_FOUND);
            return;
        }

        sendNoContent(ex);
    }

    private Long parseId(String idPart) {
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<String> validateMovie(Movie movie) {
        List<String> errors = new ArrayList<>();

        if (movie == null) {
            errors.add("Тело запроса отсутствует");
            return errors;
        }

        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            errors.add("Название не должно быть пустым");
        } else if (movie.getTitle().length() > 100) {
            errors.add("Название не должно быть длиннее 100 символов");
        }

        int maxYear = Year.now().getValue() + 1;

        if (movie.getYear() < MIN_YEAR || movie.getYear() > maxYear) {
            errors.add("Год должен быть между " + MIN_YEAR + " и " + maxYear);
        }

        return errors;
    }
}