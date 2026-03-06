package dataaccess;

import server.packages.ChessGameData;

import java.util.*;

public class Database {
    public static Map<Integer, ChessGameData> gamesDatabase = new HashMap<>();
    public static Map<String, String> authTokensDatabase = new HashMap<>();
    public static Map<String, String> passwordDatabase = new HashMap<>();

    public static void dumpDatabase() {
        gamesDatabase.clear();
        authTokensDatabase.clear();
        passwordDatabase.clear();
    }

    public static boolean usernameExists(String username) {
        return passwordDatabase.containsKey(username);
    }
}
