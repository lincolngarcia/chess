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
    static int portNumber;
    static WsClient session;
    static String sessionToken = null;
    static UiType uiStatus = UiType.PreLogin;

    static ChessGame.TeamColor teamColor = null;

    // An extra thread for waiting on messages
    static Thread t;

    enum UiType {
        PreLogin,
        PostLogin,
        Gameplay,
        Observe
    }

    static String[] commandOptions = new String[]{
            "help",
            "quit",
            "login",
            "register"
    };

    public ServerFacade(int port) {
        // Initialize the pre-login
        ServerFacade.portNumber = port;
        ServerFunctions.portNumber = ServerFacade.portNumber;


        TUI.clear();
        TUI.write("Welcome to my CS240 Chess Project");

    }

    public void run() {

        while (true) {
            String command = TUI.awaitPrompt("please enter a command:", commandOptions);
            String commandType = command.split(" ")[0].toLowerCase();

            switch (ServerFacade.uiStatus) {
                case PreLogin -> {
                    if (!ServerFacadeHelpers.handlePreLogin(command, commandType)) {
                        return;
                    }
                }
                case PostLogin -> {
                    if (!ServerFacadeHelpers.handlePostLogin(command, commandType)) {
                        return;
                    }
                }
                case Gameplay -> {
                    if (!ServerFacadeHelpers.handleGameCommand(command, commandType)) {
                        return;
                    }
                }

                case Observe -> {
                    if (!ServerFacadeHelpers.handleObservation(command, commandType)) {
                        return;
                    }
                }
            }
        }
    }

    public static void handleJoinGame(String[] args) {
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
        HttpResponse<String> joinResponse = makeRequest("/game", "PUT", ServerFacade.sessionToken, joinData);

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

            ServerFacade.session = new WsClient(
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
        ServerFacade.activateObservationalThread();
    }

    public static void handleObserveGame(String[] args) {
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

            ServerFacade.session = new WsClient(
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
        ServerFacade.activateObservationalThread();
    }

    public static String listGames() {
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

    public static JsonArray getGames() {
        HttpResponse<String> listResponse = makeRequest("/game", "GET", ServerFacade.sessionToken, null);

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(listResponse.body(), JsonObject.class);
        return json.getAsJsonArray("games");
    }

    public static void enablePostLoginUI(String sessionToken) {
        ServerFacade.sessionToken = sessionToken;
        ServerFacade.uiStatus = UiType.PostLogin;

        commandOptions = new String[]{
                "help",
                "logout",
                "create",
                "list",
                "join",
                "observe"
        };

    }

    public static void disablePostLoginUI() {
        ServerFacade.uiStatus = UiType.PreLogin;
        ServerFacade.sessionToken = null;
        commandOptions = new String[]{
                "help",
                "quit",
                "login",
                "register"
        };
    }

    public static void enableGameplayUI() {
        ServerFacade.uiStatus = UiType.Gameplay;
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

    public static void enableObservationUI() {
        ServerFacade.uiStatus = UiType.Observe;
        commandOptions = new String[]{
                "help",
                "leave",
                "switch",
                "list",
                "redraw",
                "quit"
        };
    }

    public static boolean quit() {
        TUI.write(
                EscapeSequences.format("Thanks for playing", new String[]{
                        EscapeSequences.SET_TEXT_COLOR_BLUE
                }));

        if (ServerFacade.session != null) {
            ServerFacade.leaveGame();
        }

        return false;
    }

    public static void leaveGame() {
        // Leave the game
        TUI.write(
                EscapeSequences.format(
                        "leaving game...",
                        new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                )
        );

        // Reset the UI
        enablePostLoginUI(ServerFacade.sessionToken);

        // close the connection
        try {
            // Stop waiting on the thread
            if (ServerFacade.t != null) {
                ServerFacade.t.interrupt();
                ServerFacade.t = null;
            }
            System.out.flush();

            ServerFacade.session.send(
                    new UserGameCommand(
                            UserGameCommand.CommandType.LEAVE,
                            ServerFacade.session.authToken,
                            ServerFacade.session.gameId
                    )
            );
            ServerFacade.session.session.close();
            ServerFacade.session = null;
        } catch (IOException e) {
            TUI.error("Error while closing the session");
        }
    }

    public static void activateObservationalThread() {
        ServerFacade.t = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    ServerFacade.session.awaitMessage(true);
                    TUI.prompt(
                            "please enter a command:",
                            commandOptions
                    );
                }
            } catch (InterruptedException e) {
                return;
            }
        });
        ServerFacade.t.start();
    }

    public static void sendAndReceiveBlockingMessage(UserGameCommand command) {
        // Temporarily lock the thread
        WsClient.exclusiveReceiver = Thread.currentThread().getName();
        // send the message
        ServerFacade.session.send(command);
        // Receive the response
        try {
            ServerFacade.session.awaitMessage(false);
        } catch (InterruptedException e) {
            TUI.error("Thread interrupted unexpectedly");
        }
        // unlock the thread
        WsClient.exclusiveReceiver = null;
    }

    public static void listMoves(String command, ChessGameData data) {
        String[] args = command.split(" ");

        // Assert argument count is 2
        if (args.length != 2) {
            TUI.error("Invalid arguments, try command 'help'");
            return;
        }

        // Assert string length is 2
        if (args[1].length() != 2) {
            TUI.error("Invalid arguments, try command 'help'");
            return;
        }

        // Assert the cords are valid
        int[] cords = ChessFunctions.parseLocationString(args[1]);

        if (Arrays.equals(cords, new int[]{-1, -1})) {
            TUI.error("Invalid arguments, try command 'help'");
            return;
        }

        ChessPosition position = new ChessPosition(cords[0], cords[1]);

        // Assert there is a piece in that position
        if (data.game.getBoard().getPiece(position) == null) {
            TUI.error("No piece found for given position");
            return;
        }


        Collection<ChessMove> validMoves = WsClient.lastReceivedData.game.validMoves(position);
        List<ChessPosition> highlights = null;
        if (!validMoves.isEmpty()) {
            highlights = validMoves.stream()
                    .map(ChessMove::getEndPosition)
                    .collect(Collectors.toList());
            highlights.add(position);
        }

        TUI.printBoard(data, teamColor, highlights);
    }
}
