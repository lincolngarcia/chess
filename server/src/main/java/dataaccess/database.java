package dataaccess;

import chess.ChessGame;

import java.util.*;

public class database {
    public static Map<String, ChessGame> games_db = new HashMap<>();
    public static Map<String, String> authTokens_db = new HashMap<>();
    public static Map<String, String> password_db = new HashMap<>();
    public static Map<String, String[]> players = new HashMap<>();

    public static void dump_db() {
        games_db.clear();
        authTokens_db.clear();
    }

    public static boolean usernameExists(String gameName) {
        return authTokens_db.containsKey(gameName);
    }
}
