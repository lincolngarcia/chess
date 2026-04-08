package server;

import chess.ChessMove;
import chess.InvalidMoveException;
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

    public static void handleWebSocketRequest(WsContext ctx, UserGameCommand command) throws DataAccessException {
        if (!connections.containsKey(ctx.sessionId())) {
            throw new AssertionError("Invalid Session ID");
        }

        int gameId = command.getGameID();

        if (DatabaseService.isInvalidAuth(command.getAuthToken())) {
            throw new DataAccessException("Invalid AuthToken");
        }

        ChessGameData gameData = DatabaseService.getGameById(gameId);

        switch (command.getCommandType()) {
            // Connect to a game
            case CONNECT -> {
                // Store what game the session is in
                connections.get(ctx.sessionId()).gameId = gameId;

                // Send the game
                ServerMessage msg = new ServerMessage(
                        ServerMessage.ServerMessageType.LOAD_GAME, GSON.toJson(gameData)
                );
                sendMessage(ctx, msg);

                // broadcast to others that they have joined
                String username = DatabaseService.getUsernameByAuthToken(command.getAuthToken());
                String teamColor = command.getData();
                ServerMessage alert = new ServerMessage(
                        ServerMessage.ServerMessageType.NOTIFICATION,
                        username + " has joined the game as " + teamColor
                );
                broadcastUniqueMessage(alert, connections.get(ctx.sessionId()).gameId, ctx);
            }

            // Execute a move
            case MAKE_MOVE -> {
                // get the move
                if (command.getData() == null) {
                    throw new AssertionError("Invalid Data");
                }

                // Assert the correct teams turn is to play

                // Assert correct player is making the move

                ChessMove move = GSON.fromJson(command.getData(), ChessMove.class);

                try {
                    gameData.game.makeMove(move);
                    DatabaseService.updateGame(gameData);
                } catch (InvalidMoveException e) {
                    sendMessage(ctx, new ServerMessage(
                            ServerMessage.ServerMessageType.ERROR,
                            "Invalid move"
                    ));
                    return;
                }

                broadcastMessage(
                        new ServerMessage(
                                ServerMessage.ServerMessageType.LOAD_GAME,
                                GSON.toJson(gameData)),
                        connections.get(ctx.sessionId()).gameId
                );
            }

            // leave a game
            case LEAVE -> {
                // TODO: potential pitfall: repeated usernames may cause unexpected behavior
                // easy fix? deny same username entering a game;

                // Remove the player from the database
                String username = DatabaseService.getUsernameByAuthToken(command.getAuthToken());

                Integer teamColorIndex = null;
                if (Objects.equals(gameData.playerUsernames[0], username)) {
                    teamColorIndex = 0;
                }
                if (Objects.equals(gameData.playerUsernames[1], username)) {
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
                            "resignation not accepted due to user not existing"
                    ));
                    return;
                }

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
}
