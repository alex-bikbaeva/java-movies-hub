package ru.practicum.moviehub.http;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.ErrorResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8";
    protected static final Gson GSON = new Gson();

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, response.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendNoContent(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }

    protected void sendError(HttpExchange ex, int status, String error) throws IOException {
        sendJson(ex, status, GSON.toJson(new ErrorResponse(error)));
    }

    protected void sendValidationError(HttpExchange ex, List<String> details) throws IOException {
        sendJson(ex, 422, GSON.toJson(new ErrorResponse("Ошибка валидации", details)));
    }

    protected String readRequestBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected boolean hasJsonContentType(HttpExchange ex) {
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");
        return contentType != null && contentType.toLowerCase().startsWith("application/json");
    }

    protected <T> T parseJsonBody(HttpExchange ex, Class<T> clazz) throws IOException, JsonSyntaxException {
        return GSON.fromJson(readRequestBody(ex), clazz);
    }
}