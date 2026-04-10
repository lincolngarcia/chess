package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import websocket.ChessGameData;

import java.sql.PreparedStatement;
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
                    game TEXT NOT NULL,
                    gameState TEXT
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
        String statement = """
                SELECT 1 FROM passwords WHERE username = ?;
                """;

        try (var conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

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
        String gameJson = gson.toJson(data.game);

        String whitePlayer = data.playerUsernames.length > 0 ? data.playerUsernames[0] : "";
        String blackPlayer = data.playerUsernames.length > 1 ? data.playerUsernames[1] : "";

        String sql = """
        INSERT INTO game_data (gameId, gameName, whiteUsername, blackUsername, game, gameState)
        VALUES (?, ?, ?, ?, ?, ?);
        """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, data.gameID);
            ps.setString(2, data.gameName);
            ps.setString(3, whitePlayer);
            ps.setString(4, blackPlayer);
            ps.setString(5, gameJson);
            ps.setString(6, "ACTIVE");

            ps.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error inserting game data", e);
        }
    }

    public static ChessGameData getGameById(Integer gameId) {
        String statement = """
                SELECT * FROM game_data WHERE gameId = ?;
                """;

        try (var conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Gson gson = new Gson();

                String whiteUsername = !Objects.equals(rs.getString("whiteUsername"), "null") ?
                        rs.getString("whiteUsername") : null;
                String blackUsername = !Objects.equals(rs.getString("blackUsername"), "null") ?
                        rs.getString("blackUsername") : null;

                ChessGameData data = new ChessGameData(
                        rs.getInt("gameId"),
                        rs.getString("gameName"),
                        whiteUsername,
                        blackUsername,
                        gson.fromJson(rs.getString("game"), ChessGame.class)
                );
                data.gameState = Objects.equals(rs.getString("gameState"), "ACTIVE") ? ChessGameData.GameStates.ACTIVE : ChessGameData.GameStates.INACTIVE;

                return data;
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

        String statement = """
                UPDATE game_data SET
                    gameId = ?,
                    gameName = ?,
                    whiteUsername = ?,
                    blackUsername = ?,
                    game = ?,
                    gameState = ?
                WHERE gameId = ?
                """;

        try (var conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setInt(1, data.gameID);
            ps.setString(2, data.gameName);
            ps.setString(3, data.playerUsernames[0]);
            ps.setString(4, data.playerUsernames[1]);
            ps.setString(5, game);
            ps.setString(6, data.gameState.name());
            ps.setInt(7, data.gameID);
            ps.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean doesGameExist(Integer gameId) {
        String statement = """
                SELECT 1 FROM game_data WHERE gameId = ?;
                """;

        try (var conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();

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
        String statement = """
                SELECT * FROM auth_tokens WHERE authToken = ?;
                """;

        try (var conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setString(1, authToken);
            ResultSet rs = ps.executeQuery();

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
        String statement = """
                SELECT 1 FROM auth_tokens WHERE authToken = ?;
                """;

        try (var conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setString(1, authToken);
            ResultSet rs = ps.executeQuery();
            return !rs.next();

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public static void createSession(String authToken, String username) {
        String statement = """
                INSERT INTO auth_tokens (authToken, username) VALUES (?, ?);
                """;

        try (var conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setString(1, authToken);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void logoutSession(String authToken) {
        String statement = """
                DELETE FROM auth_tokens WHERE authToken = ?;
                """;
        try (var conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setString(1, authToken);
            ps.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createUser(String username, String password) {
        String statement = """
                INSERT INTO passwords (username, password) VALUES (?, ?);
                """;

        try (var conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isValidLoginRequest(String username, String password) {
        String statement = """
                SELECT EXISTS (SELECT 1 FROM passwords WHERE username = ? AND password = ?);
                """;
        try (var conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 1;
            }

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
