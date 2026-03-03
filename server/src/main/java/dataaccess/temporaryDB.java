package dataaccess;

import chess.ChessGame;

import java.util.*;

public class temporaryDB {
    Map<String, ChessGame> games_db = new HashMap<>();
    Map<String, UUID> authTokens_db = new HashMap<>();
    Map<String, String> password_db = new HashMap<>();
    List<String> usernames = new ArrayList<>();
}
