package server;

import chess.ChessGame;
import io.javalin.*;

import java.util.*;

public class Server {



    private final Javalin javalin;

    public Server() {

        // Register your endpoints and exception handlers here.
        javalin = Javalin.create(config -> {
            config.routes.post("/register", ctx -> {
                // Register user
                this.handleRegister();
            });

            // Log in User
            config.routes.post("/login", ctx -> {
                // Register user
                this.handleLogin();
            });

            // Log out User
            config.routes.delete("/session", ctx -> {
                // Register user
                this.handleLogout();
            });

            // List game data
            config.routes.get("/game", ctx -> {
                // Register user
                this.handleGetAllGames();
            });

            // New game
            config.routes.post("/game", ctx -> {
                // Register user
                this.handleCreateGame();
            });

            // Join Game
            config.routes.put("/game", ctx -> {
                // Register user
                this.handleJoinGame();
            });

            // Clear all db data
            config.routes.delete("/db", ctx -> {
                // Register user
                this.handleDump();
            });

            config.staticFiles.add("web");
        });


    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    public ChessGame createNewGame(String gameName) {
        this.games_db.put(gameName, new ChessGame());
        return this.games_db.get(gameName);
    }

    public ChessGame getGameByName(String gameName) {
        return this.games_db.get(gameName);
    }

    public void dump_db() {
        this.games_db.clear();
        this.authTokens_db.clear();
    }

    public boolean usernameExists(String gameName) {
        return this.usernames.contains(gameName);
    }
}
