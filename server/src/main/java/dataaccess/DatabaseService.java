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

}
