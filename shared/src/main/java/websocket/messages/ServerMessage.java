package websocket.messages;

import chess.ChessGame;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    String message;
    ChessGame game;
    String errorMessage;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, String content) {
        this.serverMessageType = type;
        this.message = content;
    }

    public ServerMessage(ServerMessageType type, ChessGame game) {
        this.serverMessageType = type;
        this.game = game;
    }

    public ServerMessage(ServerMessageType type, String errorMessage, boolean isError) {
        if (isError) {
            this.serverMessageType = type;
            this.errorMessage = errorMessage;
        }
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public String getContent() {
        return this.message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
