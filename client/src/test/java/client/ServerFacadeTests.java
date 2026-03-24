package client;

import org.junit.jupiter.api.*;
import server.Server;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        facade = new ServerFacade(0);

        int port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    @Order(1)
    @DisplayName("Init Server Facade")
    void initServerFacade() {
        assert facade.commandOptions != null;
    }

    @Test
    @Order(2)
    @DisplayName("ServerFacade Negative")
    void serverFacadeNegative() {
        assert !facade.postLogin;

    }

    @Test
    @Order(3)
    @DisplayName("ServerFacade Positive")
    void serverFacadePositive() {}
}
