package server;

import chess.*;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.DatabaseService;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(8080);

        System.out.println("♕ 240 Chess Server");

    }
}
