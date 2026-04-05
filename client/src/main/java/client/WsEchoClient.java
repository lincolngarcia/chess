package client;

import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

public class WsEchoClient extends Endpoint {
    public static Session session;
    public static String authToken;
    public static int gameId;

    public WsEchoClient(String authToken, int gameId) throws Exception {
        WsEchoClient.authToken = authToken;
        WsEchoClient.gameId = gameId;

        URI uri = new URI("ws://localhost:8080/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                ServerMessage msg = new Gson().fromJson(message, ServerMessage.class);

                if (msg.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION
                && Objects.equals(msg.getContent(), "connection successful")) {
                    try {
                        WsEchoClient.send(
                                new UserGameCommand(
                                        UserGameCommand.CommandType.CONNECT,
                                        authToken,
                                        gameId
                                )
                        );
                    } catch (IOException e) {
                        TUI.error("Websocket send message error");
                    }
                }

                if (msg.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
                    TUI.write("received board, printing non unique");
                    TUI.printBoard("WHITE");
                }
            }
        });
    }

    public static void send(UserGameCommand message) throws IOException {
        String msg = new Gson().toJson(message);
        session.getBasicRemote().sendText(msg);
    }

    public boolean isActive() {
        return session.isOpen();
    }

    // This method must be overridden, but we don't have to do anything with it
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
