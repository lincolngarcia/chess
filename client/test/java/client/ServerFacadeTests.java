package client;

import org.junit.jupiter.api.*;
import server.Server;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
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
        ServerFacade facade = new ServerFacade();
        assert true;
    }

    @Test
    @Order(2)
    @DisplayName("ServerFacade Negative")
    void serverFacadeNegative() throws InterruptedException {
//        ServerFacade

    }

    @Test
    @Order(3)
    @DisplayName("handlePostLogin Positive")
    void handlePostLoginPositive() throws InterruptedException {}

    @Test
    @Order(4)
    @DisplayName("handlePostLogin Negative")
    void handlePostLoginNegative() throws InterruptedException {}

    @Test
    @Order(5)
    @DisplayName("handlePreLogin Positive")
    void handlePreLoginPositive() throws InterruptedException {}

    @Test
    @Order(6)
    @DisplayName("handlePreLogin Negative")
    void handlePreLoginNegative() throws InterruptedException {}

    @Test
    @Order(7)
    @DisplayName("enablePostLoginUI Positive")
    void enablePostLoginUIPositive() throws InterruptedException {}

    @Test
    @Order(8)
    @DisplayName("enablePostLoginUI Negative")
    void enablePostLoginUINegative() throws InterruptedException {}

    @Test
    @Order(9)
    @DisplayName("disablePostLoginUI Positive")
    void disablePostLoginUIPositive() throws InterruptedException {}

    @Test
    @Order(10)
    @DisplayName("disablePostLoginUI Negative")
    void disablePostLoginUINegative() throws InterruptedException {}
}
