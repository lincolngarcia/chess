package server.packages;

import chess.ChessGame;

import java.util.Map;

public class GetAllGamesResponse {
    public int statusCode;
    public Map<Integer, ChessGameData> map;

    public GetAllGamesResponse(Map<Integer, ChessGameData> map, int code) {
        this.statusCode = code;
        this.map = map;
    }

    @Override
    public String toString() {
        return switch (this.statusCode) {
            case 400 -> "{\"message\": \"Error: unauthorized\" }";
            case 401 -> "{\"message\": \"Error: bad request\" }";
            case 403 -> "{\"message\": \"Error: forbidden\" }";
            default -> "{\"games\": " + this.map.values() + "}";
        };
    }
}
