package server;

import chess.ChessGame;
import dataaccess.DatabaseService;
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

        if (DatabaseService.usernameExists(username)) {
            return new LoginResponse(403);
        }

        String authToken = UUID.randomUUID().toString();

        DatabaseService.createUser(username, String.valueOf(password.hashCode()));
        DatabaseService.createSession(authToken, username);

        return new LoginResponse(username, authToken);
    }

    public static LoginResponse handleLogin(LoginRequest body) {
        String username = body.username;
        String password = body.password;

        if (username == null || password == null) {
            return new LoginResponse(400);
        }

        if (!DatabaseService.usernameExists(username)) {
            return new LoginResponse(401);
        }

        if (!DatabaseService.isValidLoginRequest(username, String.valueOf(password.hashCode()))) {
            return new LoginResponse(401);
        }

        String authToken = UUID.randomUUID().toString();
        DatabaseService.createSession(authToken, username);
        return new LoginResponse(username, authToken);
    }

    public static LogoutResponse handleLogout(AuthData body) {
        String authToken = body.authToken;

        if (DatabaseService.isInvalidAuth(authToken)) {
            return new LogoutResponse(401);
        }

        DatabaseService.logoutSession(authToken);
        return new LogoutResponse(200);
    }

    public static GetAllGamesResponse handleGetAllGames(AuthData body) {
        String authToken = body.authToken;

        if (DatabaseService.isInvalidAuth(authToken)) {
            return new GetAllGamesResponse(null, 401);
        }

        return new GetAllGamesResponse(DatabaseService.getAllGames(), 200);
    }

    public static CreateGameResponse handleCreateGame(AuthData body, CreateGameRequest data) {
        String authToken = body.authToken;
        String gameName = data.gameName;

        if (gameName == null) {
            return new CreateGameResponse(400);
        }

        int hash = gameName.hashCode() & 0x7FFFFFFF;
        if (DatabaseService.isInvalidAuth(authToken)) {
            return new CreateGameResponse(401);
        }

        if (DatabaseService.doesGameExist(hash)) {
            return new CreateGameResponse(400);
        }

        ChessGame newGame = new ChessGame();
        ChessGameData responseData = new ChessGameData(hash, gameName, null, null, newGame);
        DatabaseService.addGame(responseData);
        return new CreateGameResponse(gameName, hash);
    }

    public static JoinGameResponse handleJoinGame(JoinGameRequest body, AuthData authData) {
        String authToken = authData.authToken;
        int gameID = body.gameID;
        String playerColor = body.playerColor;

        if (!Objects.equals(playerColor, "WHITE") && !Objects.equals(playerColor, "BLACK")) {
            return new JoinGameResponse(400);
        }

        if (DatabaseService.isInvalidAuth(authToken)) {
            return new JoinGameResponse(401);
        }

        if (!DatabaseService.doesGameExist(gameID)) {
            return new JoinGameResponse(400);
        }

        int index = Objects.equals(playerColor, "WHITE") ? 0 : 1;
        if (DatabaseService.getGameById(gameID).playerUsernames[index] != null) {
            return new JoinGameResponse(403);
        }

        String whiteUsername = DatabaseService.getGameById(gameID).playerUsernames[0];
        String blackUsername = DatabaseService.getGameById(gameID).playerUsernames[1];

        if (index == 0) {
            whiteUsername = DatabaseService.getUsernameByAuthToken(authToken);
        }else{
            blackUsername = DatabaseService.getUsernameByAuthToken(authToken);
        }

        ChessGameData currentData = DatabaseService.getGameById(gameID);
        ChessGameData gameData = new ChessGameData(
                currentData.gameID,
                currentData.gameName,
                whiteUsername,
                blackUsername,
                currentData.game
        );

        DatabaseService.updateGame(gameData);

        return new JoinGameResponse(200);
    }

    public static DbDumpResponse handleDump() {
        DatabaseService.dumpDatabase();

        return new DbDumpResponse(200);
    }
}