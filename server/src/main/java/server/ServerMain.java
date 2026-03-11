package server;

import chess.*;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(8080);

        System.out.println("♕ 240 Chess Server");

        // See if the database is active

        // Load in any data from database

        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
