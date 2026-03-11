package dataaccess;

import server.packages.ChessGameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DatabaseService {
    public static Map<Integer, ChessGameData> gamesDatabase = new HashMap<>();
    public static Map<String, String> authTokensDatabase = new HashMap<>();

    static {
        try {
            DatabaseService.createTables();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static void createTables() throws DataAccessException {
        String statement = """
                CREATE TABLE IF NOT EXISTS passwords (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(50) NOT NULL
                    );
                """;
        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement(statement).executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Message");
        }
    }

    public static void dumpDatabase() {
        String statement = """
                DROP TABLE IF EXISTS passwords;
                """;
        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement(statement).executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        gamesDatabase.clear();
        authTokensDatabase.clear();
    }

    public static boolean usernameExists(String username) {
        String statement = String.format("""
                SELECT 1 FROM passwords WHERE username = '%s';
                """, username);

        try (var conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(statement);

            if (rs.next()) {
                return true;
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }

        return false;
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
        String statement = String.format("""
                INSERT INTO passwords (username, password) VALUES ('%s', '%s');
                """, username, password);

        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement(statement).executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isValidLoginRequest(String username, String password) {
        String statement = String.format("""
                SELECT EXISTS (SELECT 1 FROM passwords WHERE username = '%s' AND password = '%s');
                """, username, password);
        try (var conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.prepareStatement(statement).executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 1;
            }

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

}
