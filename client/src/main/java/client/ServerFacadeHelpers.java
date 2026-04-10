package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.converter.ChessFunctions;
import com.google.gson.Gson;
import ui.EscapeSequences;
import websocket.ChessGameData;
import websocket.commands.UserGameCommand;

import java.net.http.HttpResponse;
import java.util.Arrays;

import static serverfunctions.ServerFunctions.getValue;
import static serverfunctions.ServerFunctions.makeRequest;

public class ServerFacadeHelpers {

    public static boolean handlePreLogin(String command, String commandType) {
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
                    ServerFacade.enablePostLoginUI(getValue(registerResponseBody, "authToken"));
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
                    ServerFacade.enablePostLoginUI(getValue(loginResponseBody, "authToken"));
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
                return ServerFacade.quit();
            default:
                TUI.error("Invalid command: '" + commandType + "'");
                break;
        }

        return true;
    }

    public static boolean handlePostLogin(String command, String commandType) {
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
                HttpResponse<String> logoutResponse = makeRequest("/session", "DELETE", ServerFacade.sessionToken, null);
                if (logoutResponse.statusCode() == 200) {
                    TUI.write(EscapeSequences.format(
                            "you're logged out now",
                            new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                    ));
                    ServerFacade.disablePostLoginUI();
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
                HttpResponse<String> createResponse = makeRequest("/game", "POST", ServerFacade.sessionToken, createData);
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
                TUI.write(ServerFacade.listGames());
                break;
            case "join":
                // Transition to the helper function
                ServerFacade.handleJoinGame(args);
                break;
            case "observe":
                // Transition to helper function
                ServerFacade.handleObserveGame(args);
                break;
            case "quit":
                return ServerFacade.quit();
            default:
                TUI.error("Invalid command: '" + commandType + "'");
                break;
        }
        return true;
    }

    public static boolean handleGameCommand(String command, String commandType) {
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
                TUI.printBoard(data, ServerFacade.teamColor, null);
                break;
            case "leave":
                ServerFacade.leaveGame();
                break;
            case "resign":
                boolean confirmation = false;
                while (!confirmation) {
                    String resign = TUI.awaitPrompt("Are you sure?", new String[]{"yes", "no"});
                    if (resign.equals("yes")) {
                        confirmation = true;
                    }
                    if (resign.equals("no")) {

                        return true;
                    }
                }
                TUI.write("resigning game");
                ServerFacade.sendAndReceiveBlockingMessage(
                        new UserGameCommand(
                                UserGameCommand.CommandType.RESIGN,
                                ServerFacade.session.authToken,
                                ServerFacade.session.gameId
                        )
                );
                break;
            case "list":
                ServerFacade.listMoves(command, data);
                break;
            case "quit":
                return ServerFacade.quit();
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

                ServerFacade.sendAndReceiveBlockingMessage(
                        new UserGameCommand(
                                UserGameCommand.CommandType.MAKE_MOVE,
                                ServerFacade.session.authToken,
                                ServerFacade.session.gameId,
                                new Gson().toJson(move)
                        )
                );
        }

        return true;
    }

    public static boolean handleObservation(String command, String commandType) {
        ChessGameData data = WsClient.lastReceivedData;

        switch (commandType) {
            case "help":
                String helpText = """
                        help - with possible commands
                        leave - the game
                        switch - the game perspective
                        redraw - the board
                        list [start square] - all moves for position
                        quit - playing chess""";
                TUI.write(
                        EscapeSequences.format(
                                helpText,
                                new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE})
                );
                break;
            case "leave":
                ServerFacade.leaveGame();
                break;

            case "switch":
                TUI.write(
                        EscapeSequences.format(
                                "switching view angle...",
                                new String[]{EscapeSequences.SET_TEXT_COLOR_BLUE}
                        )
                );
                ServerFacade.teamColor = ChessFunctions.isWhite(ServerFacade.session.teamColor) ?
                        ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                ServerFacade.session.teamColor = ServerFacade.teamColor;
                break;

            case "redraw":
                TUI.printBoard(data, ServerFacade.teamColor, null);
                break;

            case "list":
                ServerFacade.listMoves(command, data);
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
}
