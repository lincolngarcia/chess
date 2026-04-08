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
                        ServerMessage.ServerMessageType.LOAD_GAME, new Gson().toJson(gameData)
                );
                sendMessage(ctx, msg);
            }

            // Execute a move
            case MAKE_MOVE -> {
                // get the move
                if (command.getData() == null) {
                    throw new AssertionError("Invalid Data");
                }

                // Assert the correct teams turn is to play

                // Assert correct player is making the move

                // Assert the move is valid

                System.out.println("received move");

                ChessMove move = GSON.fromJson(command.getData(), ChessMove.class);

                try {
                    gameData.game.makeMove(move);
                    DatabaseService.updateGame(gameData);
                } catch (InvalidMoveException e) {
                    System.out.println("Invalid Move");
                }
            }

            // leave a game
            case LEAVE -> {

            }

            // end the game
            case RESIGN -> {

            }
        }
    }

    private static void sendMessage(WsContext ctx, ServerMessage msg) {
        String json = GSON.toJson(msg);
        ctx.send(json);
    }

    private static void broadcastMessage(UserGameCommand command, int GameId) {
        // Serialize the command to JSON using Gson
        String json = GSON.toJson(command);
        // Send the JSON to every connected client
        for (connection ctx : connections.values()) {
            if (GameId == ctx.gameId) {
                ctx.client.send(json);
            }
        }
    }
}
