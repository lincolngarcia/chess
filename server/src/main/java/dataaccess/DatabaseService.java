package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import server.packages.ChessGameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DatabaseService {
    static {
        try {
            DatabaseService.createTables();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static void createTables() throws DataAccessException {
        String createPasswordTable = """
                CREATE TABLE IF NOT EXISTS passwords (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(50) NOT NULL
                    );""";
        String createGameDataTable = """
                CREATE TABLE IF NOT EXISTS game_data (
                    gameId INT PRIMARY KEY,
                    gameName VARCHAR(50) NOT NULL,
                    whiteUsername VARCHAR(50),
                    blackUsername VARCHAR(50),
                    game TEXT NOT NULL
                )
                """;
        String createAuthTokenTable = """
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    authToken VARCHAR(50) PRIMARY KEY,
                    username VARCHAR(50) NOT NULL
                    )
                """;

        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement(createPasswordTable).executeUpdate();
            conn.prepareStatement(createGameDataTable).executeUpdate();
            conn.prepareStatement(createAuthTokenTable).executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Create Tables Failed");
        }
    }

    public static void dumpDatabase() {
        String dropPasswordTable = """
                TRUNCATE TABLE passwords;
                """;
        String dropGameDataTable = """
                TRUNCATE TABLE game_data;
                """;
        String dropAuthTokenTable = """
                TRUNCATE TABLE auth_tokens;
                """;

        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement(dropPasswordTable).executeUpdate();
            conn.prepareStatement(dropGameDataTable).executeUpdate();
            conn.prepareStatement(dropAuthTokenTable).executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
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
        Gson gson = new Gson();
        String game = gson.toJson(data.game);
        String whitePlayer = data.playerUsernames[0];
        String blackPlayer = data.playerUsernames[1];

        String statement = String.format("""
                INSERT INTO game_data (gameId, gameName, whiteUsername, blackUsername, game) VALUES (
                %d, '%s',  '%s', '%s', '%s');
                """, data.gameID, data.gameName, whitePlayer, blackPlayer, game);

        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement(statement).executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static ChessGameData getGameById(Integer gameId) {
        String statement = String.format("""
                SELECT * FROM game_data WHERE gameId = %d;
                """, gameId);

        try (var conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.prepareStatement(statement).executeQuery();

            if (rs.next()) {
                Gson gson = new Gson();
                return new ChessGameData(
                        rs.getInt("gameId"),
                        rs.getString("gameName"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        gson.fromJson(rs.getString("game"), ChessGame.class)
                );
            } else {
                throw new DataAccessException("gameId returned no results");
            }

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateGame(ChessGameData data) {
        Gson gson = new Gson();
        String game = gson.toJson(data.game);

        String statement = String.format("""
                UPDATE game_data SET
                    gameId = '%d',
                    gameName = '%s',
                    whiteUsername = '%s',
                    blackUsername = '%s',
                    game = '%s'
                WHERE gameId = %d;
                """, data.gameID, data.gameName, data.playerUsernames[0], data.playerUsernames[1], game, data.gameID);

        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement(statement).executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean doesGameExist(Integer gameId) {
        String statement = String.format("""
                SELECT 1 FROM game_data WHERE gameId = '%d';
                """, gameId);

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

    public static Map<Integer, ChessGameData> getAllGames() {
        Map<Integer, ChessGameData> map = new HashMap<>();

        String statement = """
                SELECT * FROM game_data;
                """;

        try (var conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.prepareStatement(statement).executeQuery();
            Gson gson = new Gson();

            while (rs.next()) {
                map.put(
                        rs.getInt("gameId"),
                        new ChessGameData(
                                rs.getInt("gameId"),
                                rs.getString("gameName"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                gson.fromJson(rs.getString("game"), ChessGame.class))
                );
            }

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static String getUsernameByAuthToken(String authToken) {
        String statement = String.format("""
                SELECT * FROM auth_tokens WHERE authToken = '%s';
                """, authToken);

        try (var conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.prepareStatement(statement).executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            } else {
                throw new DataAccessException("gameId returned no results");
            }

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isInvalidAuth(String authToken) {
        String statement = String.format("""
                SELECT 1 FROM auth_tokens WHERE authToken = '%s';
                """, authToken);

        try (var conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.prepareStatement(statement).executeQuery();
            return !rs.next();

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public static void createSession(String authToken, String username) {
        String statement = String.format("""
                INSERT INTO auth_tokens (authToken, username) VALUES ('%s', '%s');
                """, authToken, username);

        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement(statement).executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void logoutSession(String authToken) {
        String statement = String.format("""
                DELETE FROM auth_tokens WHERE authToken = '%s';
                """, authToken);
        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement(statement).executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
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
