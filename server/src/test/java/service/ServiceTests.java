package service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {
    private static final int PORT_NUMBER = 49620;
    private static Server server;

    public static String authToken;
    public static String invalidAuthToken = "invalidAuthToken";
    public static String gameID;

    @BeforeAll
    public static void init() {
        ServiceTests.server = new Server();
        ServiceTests.server.run(PORT_NUMBER);
    }

    @AfterAll
    public static void stop() {
        ServiceTests.server.stop();
    }

    public static HttpResponse<String> makeRequest(String endpoint, String type, String authorization, String data) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT_NUMBER + endpoint));

        switch (type) {
            case "GET":
                requestBuilder.GET();
                break;
            case "POST", "PUT":
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(data));
                break;
            case "DELETE":
                requestBuilder.DELETE();
                break;
            default:
                throw new IllegalArgumentException("Invalid request type");
        }

        if (authorization != null) {
            requestBuilder.header("Authorization", authorization);
        }

        HttpRequest request = requestBuilder.build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getValue(String json, String getter) {
        System.out.println(json);
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        try {
            return obj.get(getter).getAsString();
        } catch (NullPointerException e) {
            return null;
        }
    }

    // Register
    @Test
    @Order(0)
    @DisplayName("Register Confirmation")
    public void registerConfirmation() {
        String data = """
                {
                  "username": "firstUser",
                  "password": "password",
                  "email": "email@example.com"
                }
                """;

        HttpResponse<String> res = makeRequest("/user", "POST", null, data);
        assert Objects.equals(getValue(res.body(), "username"), "firstUser");
    }

    @Test
    @Order(1)
    @DisplayName("Register Denial")
    public void registerDenial() {
        String data = """
                {
                  "username": "firstUser",
                  "password": "password",
                  "email": "email@example.com"
                }
                """;

        HttpResponse<String> res = makeRequest("/user", "POST", null, data);
        assert res.statusCode() != 200;
    }

    // Login
    @Test
    @Order(2)
    @DisplayName("Login Confirmation")
    public void loginConfirmation() {
        String data = """
                {
                  "username": "firstUser",
                  "password": "password"
                }
                """;

        HttpResponse<String> res = makeRequest("/session", "POST", null, data);
        assert getValue(res.body(), "authToken") != null;
        ServiceTests.authToken = getValue(res.body(), "authToken");
    }

    @Test
    @Order(3)
    @DisplayName("Login Denial")
    public void loginDenial() {
        String data = """
                {
                  "username": "firstUser",
                  "password": "invalidPassword",
                }
                """;

        HttpResponse<String> res = makeRequest("/session", "POST", null, data);
        assert res.statusCode() != 200;
    }

    // Logout
    @Test
    @Order(4)
    @DisplayName("Logout Confirmation")
    public void logoutConfirmation() {
        String loginData = """
                {
                  "username": "firstUser",
                  "password": "password"
                }
                """;

        HttpResponse<String> login = makeRequest("/session", "POST", null, loginData);
        String authToken = getValue(login.body(), "authToken");
        HttpResponse<String> response = makeRequest(
                "/session",
                "DELETE",
                authToken,
                null
        );
        assert response.statusCode() == 200;
    }

    @Test
    @Order(5)
    @DisplayName("Logout Denail")
    public void logoutDenial() {
        HttpResponse<String> res = makeRequest("/session", "DELETE", invalidAuthToken, null);
        assert res.statusCode() != 200;
    }

    // List Games
    @Test
    @Order(6)
    @DisplayName("List Games Confirmation")
    public void listGamesConfirmation() {
        HttpResponse<String> res = makeRequest("/game", "GET", authToken, null);
        assert res.statusCode() == 200;
    }

    @Test
    @Order(7)
    @DisplayName("List Games Denial")
    public void listGamesDenial() {
        HttpResponse<String> res = makeRequest("/game", "GET", invalidAuthToken, null);
        assert res.statusCode() != 200;
    }

    // Create Game
    @Test
    @Order(8)
    @DisplayName("Create Game Confirmation")
    public void createGameConfirmation() {
        String data = """
                {"gameName":  "firstGame"}
                """;
        HttpResponse<String> res = makeRequest("/game", "POST", authToken, data);
        assert res.statusCode() == 200;
        gameID = getValue(res.body(), "gameID");
    }

    @Test
    @Order(9)
    @DisplayName("Create Game Denial")
    public void createGameDenial() {
        String data = """
                {"gameName":  "firstGame"}
                """;
        HttpResponse<String> res = makeRequest("/game", "POST", authToken, data);
        assert getValue(res.body(), "gameID") == null;
    }

    // Join Game
    @Test
    @Order(10)
    @DisplayName("Join Game Confirmation")
    public void joinGameConfirmation() {
        String data = String.format("""
                {"gameID":  "%s", "playerColor": "BLACK"}
                """, gameID);
        HttpResponse<String> res = makeRequest("/game", "PUT", authToken, data);
        assert getValue(res.body(), "gameID") == null;
    }

    @Test
    @Order(11)
    @DisplayName("Join Game Denial")
    public void joinGameDenial() {
        String data = String.format("""
                {"gameID":  "%s", "playerColor": "RED"}
                """, gameID);
        HttpResponse<String> res = makeRequest("/game", "PUT", authToken, data);
        assert res.statusCode() != 200;
    }

    // DB Dump
    @Test
    @Order(12)
    @DisplayName("DB Dump Confirmation")
    public void dbDumpConfirmation() {
        HttpResponse<String> res = makeRequest("/db", "DELETE", authToken, null);
        assert res.statusCode() == 200;
    }

    @Test
    @Order(13)
    @DisplayName("DB Dump Denial")
    public void dbDumpDenial() {
        HttpResponse<String> res = makeRequest("/db", "DELETE", authToken, null);
        assert res.statusCode() != 400;
    }
}
