package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.converter.ChessFunctions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import serverfunctions.ServerFunctions;
import ui.EscapeSequences;
import websocket.ChessGameData;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

import static serverfunctions.ServerFunctions.getValue;
import static serverfunctions.ServerFunctions.makeRequest;

public class ServerFacade {
    int portNumber;
    WsClient session;
    String sessionToken = null;
    UiType UiStatus = UiType.PreLogin;

    ChessGame.TeamColor teamColor = null;

    // An extra thread for waiting on messages
    Thread T;

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
            String command = TUI.awaitPrompt("please enter a command:", commandOptions);
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
                    if (!handleGameCommand(command, commandType)) {
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
                return quit();
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
                return quit();
            default:
                TUI.error("Invalid command: '" + commandType + "'");
                break;
        }
        return true;
    }

    private boolean handleGameCommand(String command, String commandType) {
        String formatted;

        ChessGameData data = WsClient.lastReceivedData;
        if (data == null) {
            TUI.error("No received chess data to display");
            return true;
        }

        switch (commandType) {
            case "help":
                String helpText = """
                        help - with possible commands
                        [start/end square] - perform move
                        redraw - the board
                        leave - the game
                        resign - the game
                        list [start square] - all moves for position
                        quit - playing chess""";
                formatted = EscapeSequences.format(helpText, new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE});
                TUI.write(formatted);
                break;
            case "redraw":
                TUI.printBoard(data, teamColor, null);
                break;
            case "leave":
                this.leaveGame();
                break;
            case "resign":
                TUI.write("resigning game");
                this.sendAndReceiveBlockingMessage(
                        new UserGameCommand(
                                UserGameCommand.CommandType.RESIGN,
                                this.session.authToken,
                                this.session.gameId
                        )
                );
                break;
            case "list":
                String[] args = command.split(" ");

                // Assert argument count is 2
                if (args.length != 2) {
                    TUI.error("Invalid arguments, try command 'help'");
                    return true;
                }

                // Assert string length is 2
                if (args[1].length() != 2) {
                    TUI.error("Invalid arguments, try command 'help'");
                    return true;
                }

                // Assert the cords are valid
                int[] cords = ChessFunctions.parseLocationString(args[1]);

                if (Arrays.equals(cords, new int[]{-1, -1})) {
                    TUI.error("Invalid arguments, try command 'help'");
                    return true;
                }

                ChessPosition position = new ChessPosition(cords[0], cords[1]);
                Collection<ChessMove> validMoves = WsClient.lastReceivedData.game.validMoves(position);
                List<ChessPosition> highlights = null;
                if (!validMoves.isEmpty()) {
                    highlights = validMoves.stream()
                            .map(ChessMove::getEndPosition)
                            .collect(Collectors.toList());
                }

                TUI.printBoard(data, teamColor, highlights);
                break;
            case "quit":
                return quit();
            default:
                if (commandType.length() != 4) {
                    TUI.error("Invalid command: '" + commandType + "'");
                    return true;
                }

                String startingSquare = commandType.substring(0, 2);
                String endingSquare = commandType.substring(2);

                int[] startingCords = ChessFunctions.parseLocationString(startingSquare);
                int[] endingCords = ChessFunctions.parseLocationString(endingSquare);

                if (Arrays.equals(startingCords, new int[]{-1, -1})) {
                    TUI.error("Invalid command: '" + commandType + "'");
                    return true;
                }

                if (Arrays.equals(endingCords, new int[]{-1, -1})) {
                    TUI.error("Invalid command: '" + commandType + "'");
                    return true;

                }

                ChessMove move = new ChessMove(
                        new ChessPosition(startingCords[0], startingCords[1]),
                        new ChessPosition(endingCords[0], endingCords[1])
                );

                this.sendAndReceiveBlockingMessage(
                        new UserGameCommand(
                                UserGameCommand.CommandType.MAKE_MOVE,
                                session.authToken,
                                session.gameId,
                                new Gson().toJson(move)
                        )
                );
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
                this.leaveGame();
                break;

            case "switch":
                TUI.write(
                        EscapeSequences.format(
                                "switching view angle...",
                                new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                        )
                );
                this.teamColor = ChessFunctions.isWhite(this.session.teamColor) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                this.session.teamColor = this.teamColor;
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
            if (Objects.equals(args[2].toLowerCase(), "white")) {
                teamColor = ChessGame.TeamColor.WHITE;
            } else if (Objects.equals(args[2].toLowerCase(), "black")) {
                teamColor = ChessGame.TeamColor.BLACK;
            } else {
                TUI.error("Invalid team color");
                return;
            }

            UserGameCommand.UserGameState userGameState;

            if (Objects.equals(args[2], "WHITE")) {
                userGameState = UserGameCommand.UserGameState.WHITE;
            } else if (Objects.equals(args[2], "BLACK")) {
                userGameState = UserGameCommand.UserGameState.BLACK;
            } else {
                TUI.error("Invalid team color");
                return;
            }

            this.session = new WsClient(
                    sessionToken,
                    gameId,
                    teamColor,
                    portNumber,
                    userGameState
            );

        } catch (Exception e) {
            TUI.error("Connection Error");
            return;
        }

        // Transition to gameplayUI
        enableGameplayUI();

        // Enable the observational thread
        this.activateObservationalThread();
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
                    portNumber,
                    UserGameCommand.UserGameState.OBSERVER
            );
        } catch (Exception e) {
            TUI.error("Connection Error");
        }

        // Transition to observationUI
        enableObservationUI();

        // Enable a second thread that awaits for messages
        this.activateObservationalThread();
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
                "list",
                "[move]",
                "quit"
        };
    }

    private void enableObservationUI() {
        this.UiStatus = UiType.Observe;
        commandOptions = new String[]{
                "help",
                "leave",
                "switch",
                "quit"
        };
    }

    private boolean quit() {
        TUI.write(
                EscapeSequences.format("Thanks for playing", new String[]{
                        EscapeSequences.SET_TEXT_COLOR_BLUE
                }));

        if (this.session != null) {
            this.leaveGame();
        }

        return false;
    }

    private void leaveGame() {
        // Leave the game
        TUI.write(
                EscapeSequences.format(
                        "leaving game...",
                        new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                )
        );

        // Reset the UI
        enablePostLoginUI(this.sessionToken);

        // close the connection
        try {
            // Stop waiting on the thread
            if (this.T != null) {
                this.T.interrupt();
                this.T = null;
            }
            System.out.flush();

            this.session.send(
                    new UserGameCommand(
                            UserGameCommand.CommandType.LEAVE,
                            this.session.authToken,
                            this.session.gameId
                    )
            );
            this.session.session.close();
            this.session = null;
        } catch (IOException e) {
            TUI.error("Error while closing the session");
        }
    }

    private void activateObservationalThread() {
        this.T = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    this.session.awaitMessage(true);
                    TUI.prompt(
                            "please enter a command:",
                            commandOptions
                    );
                }
            } catch (InterruptedException e) {
                return;
            }
        });
        this.T.start();
    }

    private void sendAndReceiveBlockingMessage(UserGameCommand command) {
        // Temporarily lock the thread
        WsClient.exclusiveReceiver = Thread.currentThread().getName();
        // send the message
        this.session.send(command);
        // Receive the response
        try {
            this.session.awaitMessage(false);
        } catch (InterruptedException e) {
            TUI.error("Thread interrupted unexpectedly");
        }
        // unlock the thread
        WsClient.exclusiveReceiver = null;
    }
}
