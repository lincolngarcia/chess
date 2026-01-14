package passoff.chess;

import chess.*;
import org.junit.jupiter.api.Test;

public class ChessPersonalTests {

    @Test
    public void printChessBoard() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.println(board);
    }

    @Test
    public void printChessMove() {
        ChessMove move = new ChessMove(new ChessPosition(5, 4), ChessMove.BoardMovement.UP.value());
        System.out.println(move);
    }

    @Test
    public void printChessPiece() {
        ChessPiece piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println(piece);
    }

    @Test
    public void printChessPosition() {
        ChessPosition position = new ChessPosition(5, 4);
        System.out.println(position);
    }
}
