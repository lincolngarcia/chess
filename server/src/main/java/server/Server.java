package server;

import chess.ChessGame;
import com.google.gson.Gson;
import io.javalin.Javalin;
import server.packages.*;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {

        // Register your endpoints and exception handlers here.
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
        });

        // Register user
        javalin.post("/user", ctx -> {
            String body = ctx.body();

            RegisterRequest registerRequest = new Gson().fromJson(body, RegisterRequest.class);

            LoginResponse response = ServerApiHandler.handleRegister(registerRequest);

            ctx.status(response.statusCode);
            ctx.result(response.toString());
        });

        // Log in User
        javalin.post("/session", ctx -> {
            String body = ctx.body();

            LoginRequest loginRequest = new Gson().fromJson(body, LoginRequest.class);

            LoginResponse response = ServerApiHandler.handleLogin(loginRequest);

            ctx.status(response.statusCode);
            ctx.result(response.toString());

        });

        // Log out User
        javalin.delete("/session", ctx -> {
            AuthData authData = new AuthData(ctx.header("authorization"));

            LogoutResponse response = ServerApiHandler.handleLogout(authData);

            ctx.status(response.statusCode);
            ctx.result(response.toString());
        });

        // List game data
        javalin.get("/game", ctx -> {
            AuthData authData = new AuthData(ctx.header("authorization"));

            GetAllGamesResponse games = ServerApiHandler.handleGetAllGames(authData);

            ctx.status(games.statusCode);
            ctx.result(games.toString());
        });

        // New game
        javalin.post("/game", ctx -> {
            AuthData authData = new AuthData(ctx.header("authorization"));
            CreateGameRequest request = new Gson().fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResponse response = ServerApiHandler.handleCreateGame(authData, request);

            ctx.status(response.statusCode);
            ctx.result(response.toString());
        });

        // Join Game
        javalin.put("/game", ctx -> {
            AuthData authData = new AuthData(ctx.header("authorization"));
            JoinGameRequest joinChesGameRequest = new Gson().fromJson(ctx.body(), JoinGameRequest.class);
            JoinGameResponse response = ServerApiHandler.handleJoinGame(joinChesGameRequest, authData);

            ctx.status(response.statusCode);
            ctx.result(response.toString());
        });

        // Clear all db data
        javalin.delete("/db", ctx -> {
            AuthData authData = new AuthData(ctx.header("authorization"));

            DbDumpResponse response = ServerApiHandler.handleDump(authData);
            ctx.status(response.statusCode);
            ctx.result(" ");
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
