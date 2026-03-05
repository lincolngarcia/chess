package dataaccess;

import server.packages.ChessGameData;

import java.util.*;

public class database {
    public static Map<Integer, ChessGameData> games_db = new HashMap<>();
    public static Map<String, String> authTokens_db = new HashMap<>();
    public static Map<String, String> password_db = new HashMap<>();

    public static void dump_db() {
        games_db.clear();
        authTokens_db.clear();
        password_db.clear();
    }

    public static boolean usernameExists(String username) {
        return password_db.containsKey(username);
    }
}
