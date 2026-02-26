package chess.ChessConverter;

import chess.ChessDirection;
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

    static public int getIndexByColor(ChessGame.TeamColor color) {
        return isWhite(color) ? 0 : 1;
    }

    static public int getPawnStartRow(ChessGame.TeamColor color) {
        return isWhite(color) ? 2 : 7;
    }

    static public int getPromotionRow(ChessGame.TeamColor color) {
        return isWhite(color) ? 8 : 1;
    }

    static public int getStartRow(ChessGame.TeamColor color) {
        return isWhite(color) ? 1 : 8;
    }
}
