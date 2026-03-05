package server.packages;

public class CreateGameResponse {
    int gameId;
    String gameName;
    public int statusCode;

    public CreateGameResponse(String gameName, int gameId) {
        this.gameId = gameId;
        this.gameName = gameName;
        statusCode = 200;
    }

    public CreateGameResponse(int code) {
        this.statusCode = code;
    }

    @Override
    public String toString() {
        return switch (this.statusCode) {
            case 200 -> "{\"gameID\": " + gameId + "}";
            case 400 -> "{\"message\": \"Error: unauthorized\" }";
            case 401 -> "{\"message\": \"Error: bad request\" }";
            case 403 -> "{\"message\": \"Error: forbidden\" }";
            default -> null;
        };
    }
}
