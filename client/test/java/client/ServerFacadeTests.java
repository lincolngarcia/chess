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
        facade = new ServerFacade();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    @Order(1)
    @DisplayName("Init Server Facade")
    void initServerFacade() throws InterruptedException {
        assert facade.command_options != null;
    }

    @Test
    @Order(2)
    @DisplayName("ServerFacade Negative")
    void serverFacadeNegative() throws InterruptedException {
        assert !facade.postLogin;

    }

    @Test
    @Order(5)
    @DisplayName("handlePostLogin Positive")
    void handlePostLoginPositive() throws InterruptedException {
        assert  facade.handlePostLogin("help");
    }

    @Test
    @Order(6)
    @DisplayName("handlePostLogin Negative")
    void handlePostLoginNegative() throws InterruptedException {
        assert !facade.handlePostLogin("fakeCommand");
    }

    @Test
    @Order(3)
    @DisplayName("handlePreLogin Positive")
    void handlePreLoginPositive() throws InterruptedException {
        assert facade.handlePreLogin("help");
    }

    @Test
    @Order(4)
    @DisplayName("handlePreLogin Negative")
    void handlePreLoginNegative() throws InterruptedException {
        assert !facade.handlePreLogin("fakeCommand");
    }
}
