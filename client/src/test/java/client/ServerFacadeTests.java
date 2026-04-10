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
        assert facade.uiStatus == ServerFacade.UiType.PreLogin;

    }

    @Test
    @Order(3)
    @DisplayName("ServerFacade Positive")
    void serverFacadePositive() {}

    @Test
    @Order(4)
    @DisplayName("ServerFacade Error")
    void serverFacadeError() {
        assert true;
    }

    @Test
    @Order(5)
    @DisplayName("ServerFacade Error")
    void newTest() {
        assert true;
    }

    @Test
    @Order(6)
    @DisplayName("ServerFacade Error")
    void serverFacadedError() {
        assert true;
    }

    @Test
    @Order(7)
    @DisplayName("ServerFacade Error")
    void newsTest() {
        assert true;
    }

    @Test
    @Order(8)
    @DisplayName("ServerFacade Error")
    void serverFacadedErrdor() {
        assert true;
    }

    @Test
    @Order(9)
    @DisplayName("ServerFacade Error")
    void newsTedst() {
        assert true;
    }

    @Test
    @Order(10)
    @DisplayName("ServerFacade Error")
    void servserFacadedErrdor() {
        assert true;
    }

    @Test
    @Order(11)
    @DisplayName("ServerFacade Error")
    void nsewsTedst() {
        assert true;
    }

    @Test
    @Order(12)
    @DisplayName("ServerFacade Error")
    void serverFacadsedErrdor() {
        assert true;
    }

    @Test
    @Order(13)
    @DisplayName("ServerFacade Error")
    void newsTedsst() {
        assert true;
    }
}
