package server.packages;

import chess.ChessGame;

public class ChessGameData {
    public Integer gameID;
    public String gameName;
    public String[] playerUsernames = new String[] {null, null};
    public ChessGame game;

    public ChessGameData(Integer gameID, String gameName, String whiteUsername, String blackUsername, ChessGame game) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.playerUsernames[0] = whiteUsername;
        this.playerUsernames[1] = blackUsername;
        this.game = game;
    }

    @Override
    public String toString() {
        String whiteUsername = playerUsernames[0] != null ? ",\"whiteUsername\": \"" + this.playerUsernames[0] + "\"" : "";
        String blackUsername = playerUsernames[1] != null ? ",\"blackUsername\": \"" + this.playerUsernames[1] + "\"" : "";
        return "{\"gameID\": \"" + this.gameID + "\", \"gameName\": \"" + this.gameName + "\"" + whiteUsername + blackUsername + "}";
    }
}
