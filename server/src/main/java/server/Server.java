package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import server.packages.*;

public class Server {

    private final Javalin javalin;

    public Server() {

        // Register your endpoints and exception handlers here.
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register user
        javalin.post("/user", ctx -> {
            try {
                String body = ctx.body();

                RegisterRequest registerRequest = new Gson().fromJson(body, RegisterRequest.class);

                LoginResponse response = ServerApiHandler.handleRegister(registerRequest);

                ctx.status(response.statusCode);
                ctx.result(response.toString());
            }catch (Exception e){
                ctx.status(500);
                ctx.result("{\"message\":\"" + "Error: " + e.getMessage() + "\"}");
            }
        });

        // Log in User
        javalin.post("/session", ctx -> {
            try {
            String body = ctx.body();

            LoginRequest loginRequest = new Gson().fromJson(body, LoginRequest.class);

            LoginResponse response = ServerApiHandler.handleLogin(loginRequest);

            ctx.status(response.statusCode);
            ctx.result(response.toString());

            }catch (Exception e){
                ctx.status(500);
                ctx.result("{\"message\":\"" + "Error: " + e.getMessage() + "\"}");
            }
        });

        // Log out User
        javalin.delete("/session", ctx -> {
            try {
            AuthData authData = new AuthData(ctx.header("authorization"));

            LogoutResponse response = ServerApiHandler.handleLogout(authData);

            ctx.status(response.statusCode);
            ctx.result(response.toString());

            }catch (Exception e){
                ctx.status(500);
                ctx.result("{\"message\":\"" + "Error: " + e.getMessage() + "\"}");
            }
        });

        // List game data
        javalin.get("/game", ctx -> {
            try {
            AuthData authData = new AuthData(ctx.header("authorization"));

            GetAllGamesResponse games = ServerApiHandler.handleGetAllGames(authData);

            ctx.status(games.statusCode);
            ctx.result(games.toString());

            }catch (Exception e){
                ctx.status(500);
                ctx.result("{\"message\":\"" + "Error: " + e.getMessage() + "\"}");
            }
        });

        // New game
        javalin.post("/game", ctx -> {
            try {
            AuthData authData = new AuthData(ctx.header("authorization"));
            CreateGameRequest request = new Gson().fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResponse response = ServerApiHandler.handleCreateGame(authData, request);

            ctx.status(response.statusCode);
            ctx.result(response.toString());

            }catch (Exception e){
                ctx.status(500);
                ctx.result("{\"message\":\"" + "Error: " + e.getMessage() + "\"}");
            }
        });

        // Join Game
        javalin.put("/game", ctx -> {
            try {
            AuthData authData = new AuthData(ctx.header("authorization"));
            JoinGameRequest joinChesGameRequest = new Gson().fromJson(ctx.body(), JoinGameRequest.class);
            JoinGameResponse response = ServerApiHandler.handleJoinGame(joinChesGameRequest, authData);

            ctx.status(response.statusCode);
            ctx.result(response.toString());

            }catch (Exception e){
                ctx.status(500);
                ctx.result("{\"message\":\"" + "Error: " + e.getMessage() + "\"}");
            }
        });

        // Clear all db data
        javalin.delete("/db", ctx -> {
            try {
            DbDumpResponse response = ServerApiHandler.handleDump();
            ctx.status(response.statusCode);
            ctx.result(" ");

            }catch (Exception e){
                ctx.status(500);
                ctx.result("{\"message\":\"" + "Error: " + e.getMessage() + "\"}");
            }
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
