package server.packages;

public class LoginResponse {
    public String username;
    public String authToken;

    public int statusCode;

    public LoginResponse(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
        this.statusCode = 200;
    }

    public LoginResponse(int code) {
        this.statusCode = code;
    }

    @Override
    public String toString() {
        return switch (this.statusCode) {
            case 200 -> "{\"username\": \"" + this.username + "\",\"authToken\": \"" + this.authToken + "\"}";
            case 400 -> "{\"message\": \"Error: unauthorized\" }";
            case 401 -> "{\"message\": \"Error: bad request\" }";
            case 403 -> "{\"message\": \"Error: forbidden\" }";
            default -> null;
        };
    }
}
