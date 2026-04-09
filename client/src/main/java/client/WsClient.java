package client;

import chess.ChessGame;
import chess.converter.ChessFunctions;
import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import ui.EscapeSequences;
import websocket.ChessGameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class WsClient extends Endpoint {
    public Session session;
    public String authToken;
    public int gameId;
    public UserGameCommand.UserGameState userGameState;
    public ChessGame.TeamColor teamColor;
    private final static Queue<ServerMessage> messageQueue = new LinkedList<>();

    public static String exclusiveReceiver;

    public WsClient(String authToken, int gameId, ChessGame.TeamColor teamColor, int portNumber, UserGameCommand.UserGameState userGameState) throws Exception {
        this.authToken = authToken;
        this.gameId = gameId;
        this.teamColor = teamColor;
        this.userGameState = userGameState;

        URI uri = new URI("ws://localhost:" + portNumber + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                ServerMessage msg = new Gson().fromJson(message, ServerMessage.class);

                messageQueue.add(msg);
            }
        });

        this.send(
                new UserGameCommand(
                        UserGameCommand.CommandType.CONNECT,
                        authToken,
                        gameId,
                        new Gson().toJson(userGameState)
                )
        );

        try {
            this.awaitMessage(false);
        }catch (InterruptedException e) {
            TUI.error("Thread was interrupted unexpectedly");
        }
    }

    public void send(UserGameCommand message) {
        try {
            String msg = new Gson().toJson(message);
            session.getBasicRemote().sendText(msg);
        } catch (Exception e) {
            TUI.error("Error sending message");
        }
    }

    public void awaitMessage(boolean newLine) throws InterruptedException {
        while (true) {
            boolean queueEmpty = messageQueue.isEmpty();
            boolean isCurrentExclusiveThread = true;

            if (exclusiveReceiver != null) {
                if (!Objects.equals(Thread.currentThread().getName(), exclusiveReceiver)) {
                    isCurrentExclusiveThread = false;
                }
            }

            if (!queueEmpty && isCurrentExclusiveThread) {
                break;
            }

            Thread.sleep(10);
        }

        ServerMessage message = messageQueue.poll();
        if (message == null) {
            TUI.error("Invalid message received from queue");
            return;
        }

        this.handleMessage(message);
    }

    public void handleMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                handleLoadGame(message);
            }
            case ERROR -> {
                handleError(message);
            }
            case NOTIFICATION -> {
                handleNotification(message);
            }
        }
    }

    private void handleLoadGame(ServerMessage message) {
        ChessGameData data = new Gson().fromJson(message.getContent(), ChessGameData.class);
        // Print team to move
        String teamColorString = ChessFunctions.isWhite(data.game.getTeamTurn()) ? "White" : "Black";
        TUI.write(
                EscapeSequences.format(
                        "\n" + teamColorString + " to move",
                        new String[]{EscapeSequences.SET_TEXT_COLOR_MAGENTA}
                )
        );
        // Print board
        TUI.printBoard(data, teamColor);
    }

    private void handleError(ServerMessage message) {
        TUI.error(message.getContent());
    }

    private void handleNotification(ServerMessage message) {
        TUI.write(
                "\n" + EscapeSequences.format(
                        message.getContent(),
                        new String[]{EscapeSequences.SET_TEXT_COLOR_GREEN}
                )
        );
    }

    // This method must be overridden, but we don't have to do anything with it
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
