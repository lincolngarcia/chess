package dataaccess;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import server.Server;
import server.packages.ChessGameData;

import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessTests {
    private static final int PORT_NUMBER = 49620;
    private static Server server;

    public static String authToken;

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
            return;
        }
        assert false;
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
            return;
        }
        assert false;
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
    @DisplayName("Get username by Authtoken")
    public void  getUsernameByAuthToken() {
        String name = DatabaseService.getUsernameByAuthToken(authToken);
        assert name != null;
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
}
