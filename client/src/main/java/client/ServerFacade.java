package client;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import serverfunctions.ServerFunctions;
import ui.EscapeSequences;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static serverfunctions.ServerFunctions.getValue;
import static serverfunctions.ServerFunctions.makeRequest;

public class ServerFacade {
    UiType UiStatus = UiType.PreLogin;
    String sessionToken = null;
    ChessGame.TeamColor teamColor = null;
    int portNumber;
    WsClient session;

    enum UiType {
        PreLogin,
        PostLogin,
        Gameplay
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
                if (args.length != 3) {
                    TUI.error("Invalid arguments, try command 'help'");
                    break;
                }
                if (!handleJoinGame(args)) {
                    return false;
                }
                break;
            case "observe":
                if (args.length != 2) {
                    TUI.error("Invalid arguments, try command 'help'");
                    break;
                }
                HttpResponse<String> observeResponse = makeRequest("/game", "GET", this.sessionToken, null);
                if (observeResponse.statusCode() == 200) {
                    TUI.write(
                            EscapeSequences.format(
                                    "you are now observing this game:",
                                    new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                            )
                    );
                } else {
                    TUI.error("Invalid Request");
                }
                TUI.printBoard(args[1]);
                break;
            case "quit":
                formatted = EscapeSequences.format("Thanks for playing", new String[]{
                        EscapeSequences.SET_TEXT_COLOR_BLUE
                });
                TUI.write(formatted);
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
                        list - all moves""";
                formatted = EscapeSequences.format(helpText, new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE});
                TUI.write(formatted);
                break;
            case "redraw":
                TUI.write("Redrawing board");
                break;
            case "leave":
                TUI.write("Leaving game");
                disableGameplayUI();
                return false;
            case "resign":
                TUI.write("resigning game");
                break;
            case "list":
                TUI.write("list moves");
                break;
            default:
                ArrayList<String> validLetters = new ArrayList<>(List.of("a", "b", "c", "d", "e", "f", "g", "h"));
                ArrayList<String> validNumbers = new ArrayList<>(List.of("1", "2", "3", "4", "5", "6", "7", "8"));

                String[] splitString = commandType.split("");

                boolean validString = true;

                if (splitString.length != 4) { validString = false;}
                if (!validLetters.contains(splitString[0].toLowerCase())) {validString = false;}
                if (!validNumbers.contains(splitString[1])) {validString = false;}
                if (!validLetters.contains(splitString[2].toLowerCase())) {validString = false;}
                if (!validNumbers.contains(splitString[3])) {validString = false;}

                if (validString) {
                    TUI.write("Executing move " + commandType);
                }else{
                    TUI.error("Invalid command");
                }

                // Validate a move
                break;
        }

        return true;
    }

    private boolean handleJoinGame(String[] args) {
        String gameID = getGames().get(Integer.parseInt(args[1]) - 1).getAsJsonObject().get("gameID").getAsString();
        String joinData = "{\"playerColor\": \"" + args[2] + "\", \"gameID\": " + gameID + "}";
        HttpResponse<String> joinResponse = makeRequest("/game", "PUT", this.sessionToken, joinData);
        if (joinResponse.statusCode() == 200) {
            TUI.write(
                    EscapeSequences.format(
                            "You have joined the game",
                            new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                    ));

            enableGameplayUI();

            // Open the websocket
            try {
                this.session = new WsClient(sessionToken, Integer.parseInt(gameID));
            } catch (Exception _) {
                TUI.error("Session Failed");
                return false;
            }

        } else {
            TUI.error("Invalid Request");
            return false;
        }

        return true;
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
            builder.append(String.format("%d. %s W: %s, B: %s", i + 1, gameName,  whiteUsername, blackUsername));
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
        commandOptions = new String[] {
                "help",
                "redraw",
                "leave",
                "resign",
                "moves"
        };
    }

    private void disableGameplayUI() {
        enablePostLoginUI(this.sessionToken);
    }
}
