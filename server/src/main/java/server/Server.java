package server;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;

import java.util.*;

public class Server {

    private final Javalin javalin;

    public Server() {

        // Register your endpoints and exception handlers here.
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
        });

        javalin.post("/register", ctx -> {
            // Register user
            ServerApiHandler.handleRegister();
        });

        // Log in User
        javalin.post("/login", ctx -> {
            // Register user
            ServerApiHandler.handleLogin();
        });

        // Log out User
        javalin.delete("/session", ctx -> {
            // Register user
            ServerApiHandler.handleLogout();
        });

        // List game data
        javalin.get("/game", ctx -> {
            // Register user
            ServerApiHandler.handleGetAllGames();
        });

        // New game
        javalin.post("/game", ctx -> {
            // Register user
            ServerApiHandler.handleCreateGame();
        });

        // Join Game
        javalin.put("/game", ctx -> {
            // Register user
            ServerApiHandler.handleJoinGame();
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
