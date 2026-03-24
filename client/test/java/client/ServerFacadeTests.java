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
    @Order(5)
    @DisplayName("handlePostLogin Positive")
    void handlePostLoginPositive() {
        assert  facade.handlePostLogin("help", "help");
    }

    @Test
    @Order(6)
    @DisplayName("handlePostLogin Negative")
    void handlePostLoginNegative() {
        assert !facade.handlePostLogin("fakeCommand2", "fakeCommand2");
    }

    @Test
    @Order(3)
    @DisplayName("handlePreLogin Positive")
    void handlePreLoginPositive() {
        assert facade.handlePreLogin("help",  "help");
    }

    @Test
    @Order(4)
    @DisplayName("handlePreLogin Negative")
    void handlePreLoginNegative() {
        assert !facade.handlePreLogin("fakeCommand", "fakeCommand");
    }
}
