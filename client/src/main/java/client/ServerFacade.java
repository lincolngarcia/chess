package client;

import chess.ChessGame;
import chess.converter.ChessFunctions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import serverfunctions.ServerFunctions;
import ui.EscapeSequences;
import websocket.commands.UserGameCommand;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static serverfunctions.ServerFunctions.getValue;
import static serverfunctions.ServerFunctions.makeRequest;

public class ServerFacade {
    UiType UiStatus = UiType.PreLogin;
    String sessionToken = null;
    ChessGame game = null;
    ChessGame.TeamColor teamColor = null;
    int portNumber;
    WsClient session;

    enum UiType {
        PreLogin,
        PostLogin,
        Gameplay,
        Observe
    }

    String[] commandOptions = new String[]{
            "help",
            "quit",
            "login",
            "register"
    };

    public ServerFacade(int port) {
        // Initialize the pre-login
        this.portNumber = port;
        ServerFunctions.portNumber = this.portNumber;


        TUI.clear();
        TUI.write("Welcome to my CS240 Chess Project");

    }

    public void run() {

        while (true) {
            String command = TUI.prompt("please enter a command:", commandOptions);
            String commandType = command.split(" ")[0].toLowerCase();

            switch (this.UiStatus) {
                case PreLogin -> {
                    if (!handlePreLogin(command, commandType)) {
                        return;
                    }
                }
                case PostLogin -> {
                    if (!handlePostLogin(command, commandType)) {
                        return;
                    }
                }
                case Gameplay -> {
                    if (!handleGameCommand(commandType)) {
                        return;
                    }
                }

                case Observe -> {
                    if (!handleObservation(commandType)) {
                        return;
                    }
                }
            }
        }
    }

    private boolean handlePreLogin(String command, String commandType) {
        String formatted;
        String[] args = command.split(" ");

        switch (commandType) {
            case "help":
                String helpText = """
                        register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                        login <USERNAME> <PASSWORD> - to play chess
                        quit - playing chess
                        help - with possible commands""";

                formatted = EscapeSequences.format(helpText, new String[]{
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
                    TUI.write("Invalid Request");
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
                    TUI.write("Invalid Request");
                }

                break;

            case "quit":
                formatted = EscapeSequences.format("Thanks for playing", new String[]{
                        EscapeSequences.SET_TEXT_COLOR_BLUE
                });
                TUI.write(formatted);
                return false;
            default:
                TUI.error("Invalid command: '" + commandType + "'");
                break;
        }

        return true;
    }

    private boolean handlePostLogin(String command, String commandType) {
        String formatted;
        String[] args = command.split(" ");
        switch (commandType) {
            case "help":
                String helpText = """
                        create <NAME> - a game
                        list - games
                        join <ID> [WHITE|BLACK] - a game
                        logout - when you are done
                        quit - playing chess
                        observe - a game
                        help - with possible commands""";
                formatted = EscapeSequences.format(helpText, new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE});
                TUI.write(formatted);
                break;

            case "logout":
                if (args.length != 1) {
                    TUI.error("Invalid arguments, try command 'help'");
                    break;
                }
                HttpResponse<String> logoutResponse = makeRequest("/session", "DELETE", this.sessionToken, null);
                if (logoutResponse.statusCode() == 200) {
                    TUI.write(EscapeSequences.format(
                            "you're logged out now",
                            new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                    ));
                    disablePostLoginUI();
                } else {
                    TUI.error("Invalid Request");
                }
                break;

            case "create":
                if (args.length != 2) {
                    TUI.error("Invalid arguments, try command 'help'");
                    break;
                }
                String createData = "{\"gameName\": \"" + args[1] + "\"}";
                HttpResponse<String> createResponse = makeRequest("/game", "POST", this.sessionToken, createData);
                if (createResponse.statusCode() == 200) {
                    TUI.write(EscapeSequences.format("game created.", new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}));
                } else {
                    TUI.error("Invalid Request");
                }
                break;
            case "list":
                if (args.length != 1) {
                    TUI.error("Invalid arguments, try command 'help'");
                    break;
                }
                TUI.write(listGames());
                break;
            case "join":
                // Transition to the helper function
                handleJoinGame(args);
                break;
            case "observe":
                // Transition to helper function
                handleObserveGame(args);
                break;
            case "quit":
                formatted = EscapeSequences.format("Thanks for playing", new String[]{
                        EscapeSequences.SET_TEXT_COLOR_BLUE
                });
                TUI.write(formatted);
                return false;
            default:
                TUI.error("Invalid command: '" + commandType + "'");
                break;
        }
        return true;
    }

    private boolean handleGameCommand(String commandType) {
        String formatted;

        switch (commandType) {
            case "help":
                String helpText = """
                        help - with possible commands
                        start/end square - perform move
                        redraw - the board
                        leave - the game
                        resign - the game
                        list - all moves
                        quit - playing chess""";
                formatted = EscapeSequences.format(helpText, new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE});
                TUI.write(formatted);
                break;
            case "redraw":
                TUI.write("Redrawing board");
                break;
            case "leave":
                TUI.write("Leaving game");
                enablePostLoginUI(this.sessionToken);
                return false;
            case "resign":
                TUI.write("resigning game");
                break;
            case "list":
                TUI.write("list moves");
                break;
            case "quit":
                formatted = EscapeSequences.format("Thanks for playing", new String[]{
                        EscapeSequences.SET_TEXT_COLOR_BLUE
                });
                TUI.write(formatted);
                return false;
            default:
                ArrayList<String> validLetters = new ArrayList<>(List.of("a", "b", "c", "d", "e", "f", "g", "h"));
                ArrayList<String> validNumbers = new ArrayList<>(List.of("1", "2", "3", "4", "5", "6", "7", "8"));

                String[] splitString = commandType.split("");

                boolean validString = splitString.length == 4;

                if (!validLetters.contains(splitString[0].toLowerCase())) {
                    validString = false;
                }
                if (!validNumbers.contains(splitString[1])) {
                    validString = false;
                }
                if (!validLetters.contains(splitString[2].toLowerCase())) {
                    validString = false;
                }
                if (!validNumbers.contains(splitString[3])) {
                    validString = false;
                }

                if (validString) {
                    TUI.write("Executing move " + commandType);
                } else {
                    TUI.error("Invalid command");
                }

                // Validate a move
                break;
        }

        return true;
    }

    private boolean handleObservation(String commandType) {
        switch (commandType) {
            case "help":
                String helpText = """
                        help - with possible commands
                        leave - the game
                        switch - the game perspective
                        quit - playing chess""";
                TUI.write(
                        EscapeSequences.format(
                                helpText,
                                new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE})
                        );
                break;
            case "leave":
                TUI.write(
                        EscapeSequences.format(
                                "leaving game...",
                                new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                        )
                );
                this.enablePostLoginUI(this.sessionToken);
                break;

            case "switch":
                TUI.write(
                        EscapeSequences.format(
                                "switching view angle...",
                                new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                        )
                );
                this.session.teamColor = ChessFunctions.isWhite(teamColor) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                // TODO: redraw the board
                break;

            case "quit":
                TUI.write(
                        EscapeSequences.format("Thanks for playing", new String[]{
                                EscapeSequences.SET_TEXT_COLOR_BLUE
                        })
                );
                return false;

            default:
                TUI.error("Invalid command: '" + commandType + "'");
        }

        return true;
    }

    private void handleJoinGame(String[] args) {
        // Validate the request
        if (args.length != 3) {
            TUI.error("Invalid arguments, try command 'help'");
            return;
        }

        // Assert the gameIndex is an integer
        int gameIndex;
        try {
            gameIndex = Integer.parseInt(args[1]) - 1;
        } catch (NumberFormatException e) {
            TUI.error("Invalid arguments, try command 'help'");
            return;
        }

        // Find the correct gameId
        int gameId = getGames().get(gameIndex).getAsJsonObject().get("gameID").getAsInt();
        String joinData = "{\"playerColor\": \"" + args[2] + "\", \"gameID\": " + gameId + "}";

        // Join the game
        HttpResponse<String> joinResponse = makeRequest("/game", "PUT", this.sessionToken, joinData);

        if (joinResponse.statusCode() != 200) {
            TUI.error("Invalid Request");
            return;
        }

        TUI.write(
                EscapeSequences.format(
                        "You have joined the game",
                        new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                )
        );

        // Open the websocket
        try {
            if (Objects.equals(args[2], "WHITE")) {
                teamColor = ChessGame.TeamColor.WHITE;
            }
            else if (Objects.equals(args[2], "BLACK")) {
                teamColor = ChessGame.TeamColor.BLACK;
            }
            else {
                TUI.error("Invalid team color");
                return;
            }

            this.session = new WsClient(
                    sessionToken,
                    gameId,
                    teamColor,
                    portNumber
            );

        }catch (Exception e) {
            TUI.error("Connection Error");
            return;
        }

        // Send the connect message and expect a response
        this.session.send(new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                sessionToken,
                gameId
        ));
        this.session.awaitMessage();

        // Transition to gameplayUI
        enableGameplayUI();
    }

    private void handleObserveGame(String[] args) {
        if (args.length != 2) {
            TUI.error("Invalid arguments, try command 'help'");
            return;
        }

        // Assert the gameIndex is an integer
        int gameIndex;
        try {
            gameIndex = Integer.parseInt(args[1]) - 1;
        } catch (NumberFormatException e) {
            TUI.error("Invalid arguments, try command 'help'");
            return;
        }

        JsonArray games = getGames();

        // Assert the gameIndex is a valid index
        if (gameIndex - 1 >= games.size()) {
            TUI.error("Invalid game number");
            return;
        }

        // Find the correct gameId
        int gameId = games.get(gameIndex).getAsJsonObject().get("gameID").getAsInt();
        TUI.write(
                EscapeSequences.format(
                        "You are now observing this game:",
                        new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                )
        );

        // Open the websocket
        try {
            teamColor = ChessGame.TeamColor.WHITE;

            this.session = new WsClient(
                    sessionToken,
                    gameId,
                    teamColor,
                    portNumber
            );
        }catch (Exception e) {
            TUI.error("Connection Error");
        }

        // Send the connect message and expect a response
        this.session.send(new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                sessionToken,
                gameId
        ));
        this.session.awaitMessage();

        // Transition to observationUI
        enableObservationUI();
    }

    private String listGames() {
        JsonArray games = getGames();

        StringBuilder builder = new StringBuilder();
        if (games == null) {
            return null;
        }
        for (int i = 0; i < games.size(); i++) {
            JsonObject game = games.get(i).getAsJsonObject();
            String gameID = game.get("gameID").getAsString();
            String gameName = game.get("gameName").getAsString();
            String whiteUsername = "None";
            if (!game.get("whiteUsername").isJsonNull()) {
                whiteUsername = game.get("whiteUsername").getAsString();
            }
            String blackUsername = "None";
            if (!game.get("blackUsername").isJsonNull()) {
                blackUsername = game.get("blackUsername").getAsString();
            }
            builder.append(String.format("%d. %s W: %s, B: %s", i + 1, gameName, whiteUsername, blackUsername));
            if (i != games.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private JsonArray getGames() {
        HttpResponse<String> listResponse = makeRequest("/game", "GET", this.sessionToken, null);

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(listResponse.body(), JsonObject.class);
        return json.getAsJsonArray("games");
    }

    private void enablePostLoginUI(String sessionToken) {
        this.sessionToken = sessionToken;
        this.UiStatus = UiType.PostLogin;

        commandOptions = new String[]{
                "help",
                "logout",
                "create",
                "list",
                "join",
                "observe"
        };

    }

    private void disablePostLoginUI() {
        this.UiStatus = UiType.PreLogin;
        this.sessionToken = null;
        commandOptions = new String[]{
                "help",
                "quit",
                "login",
                "register"
        };
    }

    private void enableGameplayUI() {
        this.UiStatus = UiType.Gameplay;
        commandOptions = new String[]{
                "help",
                "redraw",
                "leave",
                "resign",
                "[move]"
        };
    }

    private void enableObservationUI() {
        this.UiStatus = UiType.Observe;
        commandOptions = new String[]  {
                "help",
                "leave",
                "switch",
                "quit"
        };
    }
}
