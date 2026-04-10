package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import chess.converter.ChessFunctions;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseService;
import websocket.ChessGameData;
import websocket.commands.UserGameCommand;
import io.javalin.websocket.WsContext;
import websocket.messages.ServerMessage;

import java.util.HashMap;
import java.util.Objects;


public class WebSocketHandler {

    private static final HashMap<String, connection> connections = new HashMap<>();
    private static final Gson GSON = new Gson();

    private static class connection {
        public final WsContext client;
        public int gameId;
        public UserGameCommand.UserGameState userGameState;
        public String authToken;

        connection(WsContext client, int GameId) {
            this.client = client;
            this.gameId = GameId;
        }
    }

    public static void addConnection(WsContext ctx) {
        ctx.enableAutomaticPings();
        connections.put(ctx.sessionId(), new connection(ctx, 0));
    }

    public static void removeConnection(WsContext ctx) {
        connections.remove(ctx.sessionId());
    }

    public static void handleWebSocketRequest(WsContext ctx, UserGameCommand command) {
        if (!connections.containsKey(ctx.sessionId())) {
            throw new AssertionError("Invalid Session ID");
        }

        int gameId = command.getGameID();

        if (DatabaseService.isInvalidAuth(command.getAuthToken())) {
            sendMessage(
                    ctx,
                    new ServerMessage(
                            ServerMessage.ServerMessageType.ERROR,
                            "Error: invalid auth token",
                            true
                    )
            );
            return;
        }

        // Loop the data back into itself
        ChessGameData gameData;
        try {
            gameData = DatabaseService.getGameById(gameId);
        } catch (Exception e) {
            sendMessage(
                    ctx,
                    new ServerMessage(
                            ServerMessage.ServerMessageType.ERROR,
                            "Error: no game with that ID",
                            true
                    )
            );
            return;
        }
        ChessGame game = new ChessGame(gameData.game);
        game.gameData = gameData;

        // Get the user type
        UserGameCommand.UserGameState userState = getStateByAuthTokenAndGameId(command.getAuthToken(), gameId);

        switch (command.getCommandType()) {
            // Connect to a game
            case CONNECT -> {
                // Store what game the session is in
                connections.get(ctx.sessionId()).gameId = gameId;

                // Store the user auth token
                connections.get(ctx.sessionId()).authToken = command.getAuthToken();

                // Send the game
                sendMessage(
                        ctx,
                        new ServerMessage(
                                ServerMessage.ServerMessageType.LOAD_GAME,
                                game
                        )
                );

                // broadcast to others that they have joined
                String username = DatabaseService.getUsernameByAuthToken(command.getAuthToken());
                ServerMessage alert = new ServerMessage(
                        ServerMessage.ServerMessageType.NOTIFICATION,
                        username + " has joined the game as " //+ userState
                );
                broadcastUniqueMessage(alert, connections.get(ctx.sessionId()).gameId, ctx);
            }

            // Execute a move
            case MAKE_MOVE -> {
                // get the move
                if (command.move == null) {
                    throw new AssertionError("Error: Invalid Data");
                }

                // Assert game isn't finished
                if (isGameOver(gameData)) {
                    sendMessage(
                            ctx,
                            new ServerMessage(
                                    ServerMessage.ServerMessageType.ERROR,
                                    "Error: Game is already finished",
                                    true
                            )
                    );
                    return;
                }

                // Assert correct player is making the move
                ChessGame.TeamColor activeTeam = gameData.game.getTeamTurn();
                UserGameCommand.UserGameState expectedGameState = ChessFunctions.isWhite(activeTeam) ?
                        UserGameCommand.UserGameState.WHITE : UserGameCommand.UserGameState.BLACK;

                if (expectedGameState != userState) {
                    sendMessage(
                            ctx,
                            new ServerMessage(
                                    ServerMessage.ServerMessageType.ERROR,
                                    "Error: Not your move...",
                                    true
                            )
                    );
                    return;
                }

                // Parse and execute the move
                ChessMove move = new ChessMove(
                        new ChessPosition(
                                command.move.startPosition.row,
                                command.move.startPosition.column
                        ),
                        new ChessPosition(
                                command.move.endPosition.row,
                                command.move.endPosition.column
                        )
                );

                handleMoveRequest(command, ctx, game, move);
            }

            // leave a game
            case LEAVE -> {
                // Remove the player from the database
                String username = DatabaseService.getUsernameByAuthToken(command.getAuthToken());

                Integer teamColorIndex = null;
                if (userState == UserGameCommand.UserGameState.WHITE) {
                    teamColorIndex = 0;
                }
                if (userState == UserGameCommand.UserGameState.BLACK) {
                    teamColorIndex = 1;
                }
                if (teamColorIndex != null) {
                    gameData.playerUsernames[teamColorIndex] = null;
                    DatabaseService.updateGame(gameData);
                }

                broadcastUniqueMessage(
                        new ServerMessage(
                                ServerMessage.ServerMessageType.NOTIFICATION,
                                username + " has left"
                        ),
                        connections.get(ctx.sessionId()).gameId,
                        ctx
                );

                // Remove the session from active connections
                connections.remove(ctx.sessionId());
            }

            // end the game
            case RESIGN -> {
                if (isGameOver(gameData)) {
                    sendMessage(
                            ctx,
                            new ServerMessage(
                                    ServerMessage.ServerMessageType.ERROR,
                                    "Error: game already over",
                                    true
                            )
                    );
                    return;
                }

                // Return the notification
                String username = DatabaseService.getUsernameByAuthToken(command.getAuthToken());

                Integer teamColorIndex = null;
                if (Objects.equals(gameData.playerUsernames[0], username)) {
                    teamColorIndex = 1;
                }
                if (Objects.equals(gameData.playerUsernames[1], username)) {
                    teamColorIndex = 0;
                }
                if (teamColorIndex == null) {
                    sendMessage(ctx, new ServerMessage(
                            ServerMessage.ServerMessageType.ERROR,
                            "Error: resignation not accepted due to user not existing",
                            true
                    ));
                    return;
                }

                gameData.gameState = ChessGameData.gameStates.INACTIVE;
                DatabaseService.updateGame(gameData);

                broadcastMessage(
                        new ServerMessage(
                                ServerMessage.ServerMessageType.NOTIFICATION,
                                gameData.playerUsernames[teamColorIndex] + " has won by resignation"
                        ),
                        connections.get(ctx.sessionId()).gameId
                );
            }
        }
    }

    private static void sendMessage(WsContext ctx, ServerMessage msg) {
        String json = GSON.toJson(msg);
        ctx.send(json);
    }

    private static void broadcastMessage(ServerMessage message, int GameId) {
        String json = GSON.toJson(message);
        // Send the JSON to every connected client matching gameId
        for (connection ctx : connections.values()) {
            if (GameId == ctx.gameId) {
                ctx.client.send(json);
            }
        }
    }

    private static void broadcastUniqueMessage(ServerMessage message, int GameId, WsContext sender) {
        String json = GSON.toJson(message);

        // Send the JSON to every client matching gameId except the caller
        for (connection ctx : connections.values()) {
            if (GameId == ctx.gameId) {
                if (!ctx.client.sessionId().equals(sender.sessionId())) {
                    ctx.client.send(json);
                }
            }
        }
    }

    private static void handleMoveRequest(UserGameCommand command, WsContext ctx, ChessGame game, ChessMove move) {
        ChessGameData gameData = game.gameData;

        if (isGameOver(gameData)) {
            sendMessage(
                    ctx,
                    new ServerMessage(
                            ServerMessage.ServerMessageType.ERROR,
                            "Error: Game is already finished",
                            true
                    )
            );
        }

        try {
            gameData.game.makeMove(move);
        } catch (InvalidMoveException e) {
            sendMessage(ctx, new ServerMessage(
                    ServerMessage.ServerMessageType.ERROR,
                    "Error: Invalid move",
                    true
            ));
            return;
        }

        broadcastMessage(
                new ServerMessage(
                        ServerMessage.ServerMessageType.LOAD_GAME,
                        game
                ),
                connections.get(ctx.sessionId()).gameId
        );

        broadcastUniqueMessage(
                new ServerMessage(
                        ServerMessage.ServerMessageType.NOTIFICATION,
                        "username moved"
                ),
                connections.get(ctx.sessionId()).gameId,
                ctx
        );

        String username = DatabaseService.getUsernameByAuthToken(command.getAuthToken());

        if (gameData.game.isInCheckmate(gameData.game.getTeamTurn())) {
            broadcastMessage(
                    new ServerMessage(
                            ServerMessage.ServerMessageType.NOTIFICATION,
                            username + " has won by checkmate."
                    ),
                    connections.get(ctx.sessionId()).gameId
            );
            gameData.gameState = ChessGameData.gameStates.INACTIVE;
        }

        if (gameData.game.isInStalemate(gameData.game.getTeamTurn())) {
            broadcastMessage(
                    new ServerMessage(
                            ServerMessage.ServerMessageType.NOTIFICATION,
                            "Draw by stalemate."
                    ),
                    connections.get(ctx.sessionId()).gameId
            );
            gameData.gameState = ChessGameData.gameStates.INACTIVE;
        }

        DatabaseService.updateGame(gameData);
    }

    private static boolean isGameOver(ChessGameData gameData) {
        return gameData.gameState == ChessGameData.gameStates.INACTIVE;
    }

    private static UserGameCommand.UserGameState getStateByAuthTokenAndGameId(String authToken, int gameId) {
        ChessGameData gameData = DatabaseService.getGameById(gameId);
        String username = DatabaseService.getUsernameByAuthToken(authToken);

        if (Objects.equals(gameData.playerUsernames[0], username)) {
            return UserGameCommand.UserGameState.WHITE;
        }
        if (Objects.equals(gameData.playerUsernames[1], username)) {
            return UserGameCommand.UserGameState.BLACK;
        } else {
            return UserGameCommand.UserGameState.OBSERVER;
        }
    }

    ;
}
