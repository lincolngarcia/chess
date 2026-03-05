package server.packages;

public class JoinGameResponse {
    public int statusCode;

    public JoinGameResponse(int code) {
        this.statusCode = code;
    }

    @Override
    public String toString() {
        return switch (this.statusCode) {
            case 400 -> "{\"message\": \"Error: unauthorized\" }";
            case 401 -> "{\"message\": \"Error: bad request\" }";
            case 403 -> "{\"message\": \"Error: forbidden\" }";
            default -> "{\"message\": \"\"}";
        };
    }
}
