package client;

import chess.*;

public class ClientMain {
    static void main() {
        ServerFacade facade = new ServerFacade(0);
        facade.run();
    }
}
