package client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import server.Server;
import ui.EscapeSequences;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Objects;

public class ServerFacade {
    boolean postLogin = false;
    String sessionToken = null;
    private Server server = new Server();
    int PORT_NUMBER = 8094;

    String[] command_options = new String[] {
            "help",
            "exit",
            "login",
            "register",
            "pb"
    };

    public ServerFacade() {
        // Initialize the pre-login
        this.server.run(this.PORT_NUMBER);
        TUI.clear();
        TUI.write("Welcome to my CS240 Chess Project");

        while (true) {
            String command = TUI.prompt("please enter a command:", command_options);
            String[] args = command.split(" ");
            String command_type = command.split(" ")[0].toLowerCase();

            if (!Arrays.asList(command_options).contains(command_type)) {
                TUI.error("Invalid command: '" + command_type + "'");
                continue;
            }

            String formatted;
            switch (command_type) {
                case "help":
                    formatted = EscapeSequences.format(
                            """
                                    register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                                    login <USERNAME> <PASSWORD> - to play chess
                                    quit - playing chess
                                    help - with possible commands""", new String[]{
                                    EscapeSequences.SET_TEXT_COLOR_BLUE
                            });
                    TUI.write(formatted);
                    break;

                case "register":
                    if (args.length != 4) {
                        TUI.error("Invalid arguments, try command 'help'");
                        break;
                    }

                    formatted = EscapeSequences.format("I have a passionate dislike for paperwork...", new String[]{
                            EscapeSequences.SET_TEXT_COLOR_BLUE
                    });
                    TUI.write(formatted);

                    String registerData = "{\"username\": \"" + args[1] + "\", \"password\": \"" + args[2] + "\", \"email\": \"" + args[3] + "\"}";
                    HttpResponse<String> registerResponse = makeRequest("/user", "POST", null, registerData);

                    if (registerResponse.statusCode() == 200) {
                        String registerResponseBody = registerResponse.body();
                        enablePostLoginUI(getValue(registerResponseBody, "authToken"));
                        TUI.write(
                                EscapeSequences.format(
                                        "ok fine... you're registered now.",
                                        new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                                )
                        );
                    } else {
                        TUI.write("Invalid credentials");
                    }
                    break;

                case "login":
                    if (args.length != 3) {
                        TUI.error("Invalid arguments, try command 'help'");
                        break;
                    }

                    formatted = EscapeSequences.format("not more auth... ugggghhh", new String[]{
                            EscapeSequences.SET_TEXT_COLOR_BLUE
                    });
                    TUI.write(formatted);

                    String loginData = "{\"username\": \"" + args[1] + "\", \"password\": \"" + args[2] + "\"}";
                    HttpResponse<String> loginResponse = makeRequest("/session", "POST", null, loginData);

                    if (loginResponse.statusCode() == 200) {
                        String loginResponseBody = loginResponse.body();
                        enablePostLoginUI(getValue(loginResponseBody, "authToken"));
                        TUI.write(
                                EscapeSequences.format(
                                        "ok fine... you're logged in now.",
                                        new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                                )
                        );
                    } else {
                        TUI.write("Invalid credentials");
                    }

                    break;

                case "pb":
                    formatted = EscapeSequences.format("Aw yeah! that's what I'm talking about :)", new String[]{
                            EscapeSequences.SET_TEXT_COLOR_BLUE
                    });
                    TUI.write(formatted);
                    TUI.printBoard();
                case "exit":
                    formatted = EscapeSequences.format("Thanks for playing", new String[]{
                            EscapeSequences.SET_TEXT_COLOR_BLUE
                    });
                    TUI.write(formatted);
                    this.server.stop();
                    return;
                default:
                    break;
            }

            TUI.write("");
        }

    }

    private void enablePostLoginUI(String sessionToken) {
        this.sessionToken = sessionToken;
        this.postLogin = true;

        command_options = new String[] {
                "help",
                "logout",
                "create",
                "list",
                "join",
                "observe"
        };

    }

    public HttpResponse<String> makeRequest(String endpoint, String type, String authorization, String data) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + this.PORT_NUMBER + endpoint));

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
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        try {
            return obj.get(getter).getAsString();
        } catch (NullPointerException e) {
            return null;
        }
    }
}
