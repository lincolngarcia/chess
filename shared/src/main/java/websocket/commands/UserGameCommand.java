package websocket.commands;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    private final CommandType commandType;

    private final String authToken;

    private final Integer gameID;

    public MoveData move;

    public static class MoveData {
        public static class PosData {
            public String[] columnLabels;
            public int row;
            public int column;

            PosData(String[] columnLabels, int row, int column) {
                this.columnLabels = columnLabels;
                this.row = row;
                this.column = column;
            }
        }

        public PosData startPosition;
        public PosData endPosition;

        MoveData(PosData startPosition, PosData endPosition) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }

    }

    public enum UserGameState {
        WHITE,
        BLACK,
        OBSERVER
    }

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand that)) {
            return false;
        }
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID());
    }
}
