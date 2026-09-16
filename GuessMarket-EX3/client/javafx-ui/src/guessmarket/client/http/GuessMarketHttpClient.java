package guessmarket.client.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class GuessMarketHttpClient {

    public static final String BASE_URL =
            "http://localhost:8080/guess-market";

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;
    private final List<String> requestedPaths =
            new java.util.concurrent.CopyOnWriteArrayList<>();


    public GuessMarketHttpClient() {

        this(BASE_URL);
    }


    public GuessMarketHttpClient(
            String baseUrl) {

        this.baseUrl = baseUrl;
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                Duration.ofSeconds(5)
                        )
                        .build();
        this.gson =
                new Gson();
    }


    public String getRaw(
            String path)
            throws GuessMarketHttpException {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(toUri(path))
                        .GET()
                        .build();

        return send(request);
    }


    public <T> T getJson(
            String path,
            Class<T> type)
            throws GuessMarketHttpException {

        return parseJson(
                getRaw(path),
                type
        );
    }


    public <T> T getJson(
            String path,
            Type type)
            throws GuessMarketHttpException {

        return parseJson(
                getRaw(path),
                type
        );
    }


    public String postForm(
            String path,
            Map<String, String> fields)
            throws GuessMarketHttpException {

        String body =
                encodeForm(fields);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(toUri(path))
                        .header(
                                "Content-Type",
                                "application/x-www-form-urlencoded"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        body
                                )
                        )
                        .build();

        return send(request);
    }


    public <T> T postFormJson(
            String path,
            Map<String, String> fields,
            Class<T> type)
            throws GuessMarketHttpException {

        return parseJson(
                postForm(path, fields),
                type
        );
    }


    public String postMultipartFile(
            String path,
            Map<String, String> fields,
            String fileFieldName,
            Path file)
            throws GuessMarketHttpException {

        String boundary =
                "----GuessMarketBoundary"
                        + System.currentTimeMillis();

        byte[] body =
                buildMultipartBody(
                        boundary,
                        fields,
                        fileFieldName,
                        file
                );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(toUri(path))
                        .header(
                                "Content-Type",
                                "multipart/form-data; boundary="
                                        + boundary
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofByteArray(
                                        body
                                )
                        )
                        .build();

        return send(request);
    }


    public Gson gson() {
        return gson;
    }


    public List<String> requestedPaths() {

        return requestedPaths;
    }


    public void clearRequestedPaths() {

        requestedPaths.clear();
    }


    private URI toUri(
            String path) {

        if (path == null
                ||
                path.isBlank()) {

            throw new GuessMarketHttpException(
                    "Request path cannot be empty."
            );
        }

        if (!path.startsWith("/")) {

            path = "/" + path;
        }

        return URI.create(baseUrl + path);
    }


    private String send(
            HttpRequest request)
            throws GuessMarketHttpException {

        try {

            requestedPaths.add(
                    request.uri().getPath()
                            + (request.uri().getRawQuery() == null
                            ? ""
                            : "?" + request.uri().getRawQuery())
            );

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString(
                                    StandardCharsets.UTF_8
                            )
                    );

            String body =
                    response.body() == null
                            ? ""
                            : response.body();

            if (response.statusCode()
                    >= 400) {

                throw new GuessMarketHttpException(
                        "HTTP "
                                + response.statusCode()
                                + ": "
                                + body
                );
            }

            rejectFailedJsonResponse(body);

            return body;

        } catch (GuessMarketHttpException exception) {

            throw exception;

        } catch (ConnectException exception) {

            throw new GuessMarketHttpException(
                    "Could not connect to the server at "
                            + baseUrl
                            + ".",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new GuessMarketHttpException(
                    "The HTTP request was interrupted.",
                    exception
            );

        } catch (IOException exception) {

            throw new GuessMarketHttpException(
                    "The HTTP request failed: "
                            + exception.getMessage(),
                    exception
            );
        }
    }


    private void rejectFailedJsonResponse(
            String body) {

        String trimmed =
                body.trim();

        if (!trimmed.startsWith("{")
                ||
                !trimmed.contains("\"success\"")) {

            return;
        }

        try {

            JsonObject json =
                    JsonParser.parseString(
                            trimmed
                    ).getAsJsonObject();

            if (json.has("success")
                    &&
                    json.get("success").isJsonPrimitive()
                    &&
                    json.get("success").getAsBoolean()
                    == false) {

                String message =
                        json.has("message")
                                ? json.get("message").getAsString()
                                : "The server returned an error.";

                throw new GuessMarketHttpException(
                        message
                );
            }

        } catch (GuessMarketHttpException exception) {

            throw exception;

        } catch (RuntimeException exception) {

            /*
             * Body is not a JsonResponse object.
             * Leave it for the typed parser.
             */
        }
    }


    private <T> T parseJson(
            String body,
            Class<T> type) {

        try {

            return gson.fromJson(
                    body,
                    type
            );

        } catch (JsonSyntaxException exception) {

            throw new GuessMarketHttpException(
                    "The server returned invalid JSON.",
                    exception
            );
        }
    }


    private <T> T parseJson(
            String body,
            Type type) {

        try {

            return gson.fromJson(
                    body,
                    type
            );

        } catch (JsonSyntaxException exception) {

            throw new GuessMarketHttpException(
                    "The server returned invalid JSON.",
                    exception
            );
        }
    }


    private String encodeForm(
            Map<String, String> fields) {

        if (fields == null
                ||
                fields.isEmpty()) {

            return "";
        }

        StringJoiner joiner =
                new StringJoiner("&");

        for (Map.Entry<String, String> entry :
                fields.entrySet()) {

            String value =
                    entry.getValue() == null
                            ? ""
                            : entry.getValue();

            joiner.add(
                    URLEncoder.encode(
                            entry.getKey(),
                            StandardCharsets.UTF_8
                    )
                            + "="
                            + URLEncoder.encode(
                            value,
                            StandardCharsets.UTF_8
                    )
            );
        }

        return joiner.toString();
    }


    private byte[] buildMultipartBody(
            String boundary,
            Map<String, String> fields,
            String fileFieldName,
            Path file)
            throws GuessMarketHttpException {

        try {

            StringBuilder text =
                    new StringBuilder();

            if (fields != null) {

                for (Map.Entry<String, String> entry :
                        fields.entrySet()) {

                    text.append("--")
                            .append(boundary)
                            .append("\r\n");
                    text.append(
                            "Content-Disposition: form-data; name=\""
                    );
                    text.append(entry.getKey());
                    text.append("\"\r\n\r\n");
                    text.append(
                            entry.getValue() == null
                                    ? ""
                                    : entry.getValue()
                    );
                    text.append("\r\n");
                }
            }

            String fileName =
                    file.getFileName().toString();

            text.append("--")
                    .append(boundary)
                    .append("\r\n");
            text.append(
                    "Content-Disposition: form-data; name=\""
            );
            text.append(fileFieldName);
            text.append("\"; filename=\"");
            text.append(fileName);
            text.append("\"\r\n");
            text.append(
                    "Content-Type: application/xml\r\n\r\n"
            );

            byte[] header =
                    text.toString().getBytes(
                            StandardCharsets.UTF_8
                    );

            byte[] fileBytes =
                    Files.readAllBytes(file);

            byte[] footer =
                    ("\r\n--" + boundary + "--\r\n")
                            .getBytes(
                                    StandardCharsets.UTF_8
                            );

            byte[] body =
                    new byte[
                            header.length
                                    + fileBytes.length
                                    + footer.length
                            ];

            System.arraycopy(
                    header,
                    0,
                    body,
                    0,
                    header.length
            );
            System.arraycopy(
                    fileBytes,
                    0,
                    body,
                    header.length,
                    fileBytes.length
            );
            System.arraycopy(
                    footer,
                    0,
                    body,
                    header.length + fileBytes.length,
                    footer.length
            );

            return body;

        } catch (IOException exception) {

            throw new GuessMarketHttpException(
                    "The upload file could not be read.",
                    exception
            );
        }
    }
}
