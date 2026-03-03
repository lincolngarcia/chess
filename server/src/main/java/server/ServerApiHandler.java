package server;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.database;
import server.packages.*;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ServerApiHandler {
    public static String handleRegister(RegisterRequest body) throws DataAccessException {
        String username = body.username;
        String password = body.password;

        if (database.usernameExists(username)) {
            throw new DataAccessException("Username already exists!");
        }

        String authToken = UUID.randomUUID().toString();
        database.authTokens_db.put(username, authToken);
        database.password_db.put(username, password);

        return authToken;
    }

    public static String handleLogin(LoginRequest body) throws DataAccessException {
        String username = body.username;
        String password = body.password;

        if (!database.usernameExists(username)) {
            throw new DataAccessException("Username doesn't exist");
        }

        if (!database.password_db.get(username).equals(password)) {
            throw new DataAccessException("Password doesn't match");
        }

        String authToken = UUID.randomUUID().toString();
        database.authTokens_db.put(username, authToken);
        return authToken;
    }

    public static void handleLogout(AuthData body) throws DataAccessException {
        String authToken = ""; //TODO: update
        String username = getUsernameByAuthData(authToken);

        database.authTokens_db.remove(username);
    }

    public static Map<String, ChessGame> handleGetAllGames(AuthData body) {
        return database.games_db;
    }

    public static ChessGame handleCreateGame(CreateGameRequest body) throws DataAccessException {
        String authToken = ""; //TODO: update
        String gameName = body.gameName; //TODO: update

        if (database.games_db.containsKey(gameName)) {
            throw new DataAccessException("Game already exists!");
        }

        if (database.authTokens_db.containsValue(authToken)) {
            ChessGame newGame = new ChessGame();
            database.games_db.put(gameName, newGame);
            return newGame;
        } else {
            throw new DataAccessException("User is not logged in!");
        }

    }

    public static void handleJoinGame(JoinChesGameRequest body) throws DataAccessException {
        String authToken = ""; //TODO: update
        String gameName = body.gameId; //TODO: update
        String playerColor = body.playerColor; //TODO: update

        String username = getUsernameByAuthData(authToken);

        if (database.authTokens_db.containsValue(authToken)) {
            if (database.games_db.containsKey(gameName)) {
                String[] data = database.players.get(gameName);

                int index = Objects.equals(playerColor, "WHITE") ? 0 : 1;
                data[index] = username;

            }
        } else {
            throw new DataAccessException("User is not logged in!");
        }
    }

    public static void handleDump() {
        database.dump_db();
    }

    // Helper functions
    public static String getUsernameByAuthData(String authToken) throws DataAccessException {
        if (database.authTokens_db.containsValue(authToken)) {
            for (String key : database.authTokens_db.keySet()) {
                if (database.authTokens_db.get(key).equals(authToken)) {
                    return database.authTokens_db.get(key);
                }
            }

        }
        throw new DataAccessException("User is not logged in");
    }
}
