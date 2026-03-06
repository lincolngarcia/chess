package server;

import chess.ChessGame;
import dataaccess.Database;
import server.packages.*;

import java.util.Objects;
import java.util.UUID;

public class ServerApiHandler {
    public static LoginResponse handleRegister(RegisterRequest body) {
        String username = body.username;
        String password = body.password;

        if (username == null || password == null) {
            return new LoginResponse(400);
        }

        if (Database.usernameExists(username)) {
            return new LoginResponse(403);
        }

        String authToken = UUID.randomUUID().toString();
        Database.authTokensDatabase.put(authToken, username);
        Database.passwordDatabase.put(username, password);

        return new LoginResponse(username, authToken);
    }

    public static LoginResponse handleLogin(LoginRequest body) {
        String username = body.username;
        String password = body.password;

        if (username == null || password == null) {
            return new LoginResponse(400);
        }

        if (!Database.usernameExists(username)) {
            return new LoginResponse(401);
        }

        if (!Database.passwordDatabase.get(username).equals(password)) {
            return new LoginResponse(401);
        }

        String authToken = UUID.randomUUID().toString();
        Database.authTokensDatabase.put(authToken, username);
        return new LoginResponse(username, authToken);
    }

    public static LogoutResponse handleLogout(AuthData body) {
        String authToken = body.authToken;

        if (!Database.authTokensDatabase.containsKey(authToken)) {
            return new LogoutResponse(401);
        }

        Database.authTokensDatabase.remove(authToken);
        return new LogoutResponse(200);
    }

    public static GetAllGamesResponse handleGetAllGames(AuthData body) {
        String authToken = body.authToken;

        if (!Database.authTokensDatabase.containsKey(authToken)) {
            return new GetAllGamesResponse(null, 401);
        }

        return new GetAllGamesResponse(Database.gamesDatabase, 200);
    }

    public static CreateGameResponse handleCreateGame(AuthData body, CreateGameRequest data) {
        String authToken = body.authToken;
        String gameName = data.gameName;

        if (gameName == null) {
            return new CreateGameResponse(400);
        }

        int hash = gameName.hashCode() & 0x7FFFFFFF;
        if (!Database.authTokensDatabase.containsKey(authToken)) {
            return new CreateGameResponse(401);
        }

        if (Database.gamesDatabase.containsKey(hash)) {
            return new CreateGameResponse(400);
        }

        ChessGame newGame = new ChessGame();
        ChessGameData responseData = new ChessGameData(hash, gameName, null, null, newGame);
        Database.gamesDatabase.put(hash, responseData);
        return new CreateGameResponse(gameName, hash);
    }

    public static JoinGameResponse handleJoinGame(JoinGameRequest body, AuthData authData) {
        String authToken = authData.authToken;
        int gameID = body.gameID;
        String playerColor = body.playerColor;

        if (!Objects.equals(playerColor, "WHITE") && !Objects.equals(playerColor, "BLACK")) {
            return new JoinGameResponse(400);
        }

        if (!Database.authTokensDatabase.containsKey(authToken)) {
            return new JoinGameResponse(401);
        }

        if (!Database.gamesDatabase.containsKey(gameID)) {
            return new JoinGameResponse(400);
        }

        int index = Objects.equals(playerColor, "WHITE") ? 0 : 1;
        if (Database.gamesDatabase.get(gameID).playerUsernames[index] != null) {
            return new JoinGameResponse(403);
        }

        Database.gamesDatabase.get(gameID).playerUsernames[index] = Database.authTokensDatabase.get(authToken);

        return new JoinGameResponse(200);
    }

    public static DbDumpResponse handleDump(AuthData body) {
        Database.dumpDatabase();

        return new DbDumpResponse(200);
    }
}