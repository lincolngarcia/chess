package chess.ChessConverter;

import chess.ChessGame;

public class ChessFunctions {
    static public ChessGame.TeamColor invertColor(ChessGame.TeamColor color) {
        return color.invert();
    }

    static public boolean isWhite(ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.WHITE;
    }

    static public boolean isBlack(ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.BLACK;
    }
}
