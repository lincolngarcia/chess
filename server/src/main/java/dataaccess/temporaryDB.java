package dataaccess;

import chess.ChessGame;

import java.util.*;

public class temporaryDB {
    Map<String, ChessGame> games_db = new HashMap<>();
    Map<String, UUID> authTokens_db = new HashMap<>();
    Map<String, String> password_db = new HashMap<>();
    List<String> usernames = new ArrayList<>();

    public ChessGame createNewGame(String gameName) {
        this.games_db.put(gameName, new ChessGame());
        return this.games_db.get(gameName);
    }

    public ChessGame getGameByName(String gameName) {
        return this.games_db.get(gameName);
    }

    public void dump_db() {
        this.games_db.clear();
        this.authTokens_db.clear();
    }

    public boolean usernameExists(String gameName) {
        return this.usernames.contains(gameName);
    }
}
