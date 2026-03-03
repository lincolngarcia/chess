package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import server.packages.*;

import java.util.*;

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

            String authToken = ServerApiHandler.handleRegister(registerRequest);

            String response = new Gson().toJson(authToken);

            ctx.result(response);
        });

        // Log in User
        javalin.post("/session", ctx -> {
            String body = ctx.body();

            LoginRequest loginRequest = new Gson().fromJson(body, LoginRequest.class);

            String authToken = ServerApiHandler.handleLogin(loginRequest);

            String response = new Gson().toJson(authToken);

            ctx.result(response);
        });

        // Log out User
        javalin.delete("/session", ctx -> {
            String body = ctx.body();

            System.out.println(ctx.header("authorization"));

            AuthData authData = new AuthData("authorization");

            ServerApiHandler.handleLogout(authData);

            ctx.status(200);
        });

        // List game data
        javalin.get("/game", ctx -> {
            String body = ctx.body();

            AuthData authData = new Gson().fromJson(body, AuthData.class);

            ServerApiHandler.handleGetAllGames(authData);
        });

        // New game
        javalin.post("/game", ctx -> {
            String body = ctx.body();

            CreateGameRequest createGameRequest = new Gson().fromJson(body, CreateGameRequest.class);

            ServerApiHandler.handleCreateGame(createGameRequest);
        });

        // Join Game
        javalin.put("/game", ctx -> {
            String body = ctx.body();

            JoinChesGameRequest joinChesGameRequest = new Gson().fromJson(body, JoinChesGameRequest.class);

            ServerApiHandler.handleJoinGame(joinChesGameRequest);
        });

        // Clear all db data
        javalin.delete("/db", ctx -> {
            // Register user
            ServerApiHandler.handleDump();
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
