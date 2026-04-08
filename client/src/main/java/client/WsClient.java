package client;

import chess.ChessGame;
import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import websocket.ChessGameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;

public class WsClient extends Endpoint {
    public Session session;
    public String authToken;
    public int gameId;
    public ChessGame.TeamColor teamColor;
    private final static Queue<ServerMessage> messageQueue = new LinkedList<>();

    public WsClient(String authToken, int gameId, ChessGame.TeamColor teamColor, int portNumber) throws Exception {
        this.authToken = authToken;
        this.gameId = gameId;
        this.teamColor = teamColor;

        URI uri = new URI("ws://localhost:" + portNumber + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                ServerMessage msg = new Gson().fromJson(message, ServerMessage.class);
                messageQueue.add(msg);
            }
        });
    }

    public void send(UserGameCommand message) {
        try {
            String msg = new Gson().toJson(message);
            session.getBasicRemote().sendText(msg);
        }catch(Exception e) {
            TUI.error("Error sending message");
        }
    }

    public boolean isActive() {
        return session.isOpen();
    }

    public void awaitMessage() {
        while (messageQueue.isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                TUI.error("Interrupted Thread...");
            }
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
                break;
            }
            case ERROR -> {
                handleError(message);
                break;
            }
            case NOTIFICATION -> {
                handleNotification(message);
                break;
            }
        }
    }

    private void handleLoadGame(ServerMessage message) {
        ChessGameData data = new Gson().fromJson(message.getContent(), ChessGameData.class);
        TUI.printBoard(data, teamColor);
    }

    private void handleError(ServerMessage message) {
        TUI.error(message.getContent());
    }

    private void handleNotification(ServerMessage message) {
        TUI.write("\n" + message.getContent());
    }

    // This method must be overridden, but we don't have to do anything with it
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
