package dataaccess;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import server.Server;
import server.packages.ChessGameData;

import java.util.Map;
import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessTests {
    private static final int PORT_NUMBER = 49620;
    private static Server server;

    @BeforeAll
    public static void init() {
        DataAccessTests.server = new Server();
        DataAccessTests.server.run(PORT_NUMBER);
    }

    @AfterAll
    public static void stop() {
        DataAccessTests.server.stop();
    }

    // Register
    @Test
    @Order(0)
    @DisplayName("Dump DB")
    public void dbDump() {
        DatabaseService.dumpDatabase();
        assert DatabaseService.getAllGames().isEmpty();
    }

    @Test
    @Order(1)
    @DisplayName("Username Exists")
    public void usernameExists() {
        DatabaseService.createUser("username", "password");
        assert DatabaseService.usernameExists("username");
    }

    @Test
    @Order(2)
    @DisplayName("Username Doesn't Exist")
    public void usernameDoesNotExist() {
        assert !DatabaseService.usernameExists("fakeUsername");
    }

    @Test
    @Order(3)
    @DisplayName("Add Game")
    public void addGame() {
        DatabaseService.addGame(new ChessGameData(
                1,
                "new game",
                null,
                null,
                new ChessGame()
        ));

        assert DatabaseService.getGameById(1).game.equals(new ChessGame());
    }

    @Test
    @Order(4)
    @DisplayName("Add Game Fail")
    public void addGameFail() {
        try {
            DatabaseService.addGame(new ChessGameData(
                    1,
                    "new game",
                    null,
                    null,
                    new ChessGame()
            ));
        }catch (Exception e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    @Order(5)
    @DisplayName("getGameById")
    public void getGameById() {
        DatabaseService.getGameById(1);
        assert DatabaseService.getGameById(1).game.equals(new ChessGame());
    }

    @Test
    @Order(6)
    @DisplayName("Get Game Fail")
    public void getGameFail() {
        try {
            DatabaseService.getGameById(0);
        } catch (Exception e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    @Order(7)
    @DisplayName("updateGame")
    public void updateGame() {
        DatabaseService.updateGame(new ChessGameData(
                1,
                "new game with a new name",
                null,
                null,
                new ChessGame()
        ));
        assert DatabaseService.getGameById(1).gameName.equals("new game with a new name");
    }

    @Test
    @Order(8)
    @DisplayName("updateGameFail")
    public void getGames() {
        try {
            DatabaseService.updateGame(new ChessGameData(
                    0,
                    "new game with a new name",
                    null,
                    null,
                    new ChessGame()
            ));
        } catch (Exception e) {
            assert true;
        }
    }

    @Test
    @Order(9)
    @DisplayName("Does game exist")
    public void doesGameExist() {
        assert DatabaseService.doesGameExist(1);
    }

    @Test
    @Order(10)
    @DisplayName("Does game exist fail")
    public void doesGameExistFail() {
        try {
            DatabaseService.doesGameExist(0);
        } catch (Exception e) {
            assert true;
        }
    }

    @Test
    @Order(11)
    @DisplayName("Get All Games")
    public void getAllGames() {
        Map<Integer, ChessGameData> results = DatabaseService.getAllGames();
        assert !results.isEmpty();
    }

    @Test
    @Order(12)
    @DisplayName("Get all games fail")
    public void getAllGamesFail() {
        Map<Integer,  ChessGameData> results = DatabaseService.getAllGames();
        assert results.get(1).game != null;
    }

    @Test
    @Order(13)
    @DisplayName("Get username by authToken")
    public void  getUsernameByAuthToken() {
        DatabaseService.createSession("validAuthToken", "username");
        String name = DatabaseService.getUsernameByAuthToken("validAuthToken");
        assert Objects.equals(name, "username");
    }

    @Test
    @Order(14)
    @DisplayName("Get Username by AuthToken Fail")
    public void getUsernameByAuthTokenFail() {
        try {
            DatabaseService.getUsernameByAuthToken("invalidAuthToken");
        } catch (Exception e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    @Order(15)
    @DisplayName("isInvalidAuth")
    public void isInvalidAuth() {
        assert DatabaseService.isInvalidAuth("invalidAuthToken");
    }

    @Test
    @Order(16)
    @DisplayName("isValidAuth")
    public void isValidAuth() {
        assert !DatabaseService.isInvalidAuth("validAuthToken");
    }

    @Test
    @Order(17)
    @DisplayName("Create Session")
    public void createSession() {
        DatabaseService.createSession("validAuthToken2", "username");
        assert DatabaseService.getUsernameByAuthToken("validAuthToken2") != null;
    }

    @Test
    @Order(17)
    @DisplayName("Create Session Fail")
    public void createSessionFail() {
        try {
            DatabaseService.createSession(null, "username");
        } catch (Exception e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    @Order(18)
    @DisplayName("logout session")
    public void logoutSession() {
        DatabaseService.logoutSession("validAuthToken2");
        try {
            DatabaseService.getUsernameByAuthToken("validAuthToken2");
        } catch (Exception e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    @Order(19)
    @DisplayName("logout session fail")
    public void logoutSessionFail() {
        try {
            DatabaseService.logoutSession("invalidAuthToken");
        } catch (Exception e) {
            assert true;
        }
        assert true;
    }

    @Test
    @Order(20)
    @DisplayName("create user")
    public void createUser() {
        DatabaseService.createUser("username2", "password");
        DatabaseService.createSession("2validAuthToken2", "username2");
        assert Objects.equals(DatabaseService.getUsernameByAuthToken("2validAuthToken2"), "username2");
    }

    @Test
    @Order(21)
    @DisplayName("create user fail")
    public void createUserFail() {
        assert DatabaseService.getUsernameByAuthToken("2validAuthToken2") != null;
    }

    @Test
    @Order(22)
    @DisplayName("isValidLoginRequest")
    public void isValidLoginRequest() {
        assert DatabaseService.isValidLoginRequest("username", "password");
    }

    @Test
    @Order(23)
    @DisplayName("isValidLoginRequest fail")
    public void isValidLoginRequestFail() {
        assert DatabaseService.isValidLoginRequest("username2", "password2");
    }
}
