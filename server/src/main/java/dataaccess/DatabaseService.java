package dataaccess;

import server.packages.ChessGameData;

import java.util.*;

public class DatabaseService {
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

    public static void addGame(ChessGameData data) {
        DatabaseService.gamesDatabase.put(data.gameID, data);
    }

    public static ChessGameData getGameById(Integer gameId) {
        return gamesDatabase.get(gameId);
    }

    public static boolean doesGameExist(Integer gameId)  {
        return gamesDatabase.containsKey(gameId);
    }

    public static Map<Integer, ChessGameData>  getAllGames() {
        return gamesDatabase;
    }

    public static String getUsernameByAuthToken(String authToken) {
        return authTokensDatabase.get(authToken);
    }

    public static boolean isInvalidAuth(String authToken) {
        return !authTokensDatabase.containsKey(authToken);
    }

    public static void createSession(String authToken, String username) {
        authTokensDatabase.put(authToken, username);
    }

    public static void logoutSession(String authToken) {
        authTokensDatabase.remove(authToken);
    }

    public static void createUser(String username, String password) {
        passwordDatabase.put(username, password);
    }

    public static boolean isValidLoginRequest(String username, String password) {
        return DatabaseService.passwordDatabase.get(username).equals(password);
    }

}
