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

    @Test
    public void testPositionStrings() {
        ChessPosition position1 = new ChessPosition(1, 1);
        System.out.printf("%s | %d\n", position1, position1.getBitBoardIndex());

        ChessPosition position2 = new ChessPosition(8, 8);
        System.out.printf("%s | %d\n", position2, position2.getBitBoardIndex());

        ChessPosition position3 = new ChessPosition(3, 6);
        System.out.printf("%s | %d\n", position3, position3.getBitBoardIndex());

        ChessPosition position4 = new ChessPosition(4, 5);
        System.out.printf("%s | %d\n", position4, position4.getBitBoardIndex());

        ChessPosition position5 = new ChessPosition(0);
        System.out.printf("%s | %d\n", position5, position5.getBitBoardIndex());

        ChessPosition position6 = new ChessPosition(28);
        System.out.printf("%s | %d\n", position6, position6.getBitBoardIndex());

        ChessPosition position7 = new ChessPosition(35);
        System.out.printf("%s | %d\n", position7, position7.getBitBoardIndex());

        ChessPosition position8 = new ChessPosition(63);
        System.out.printf("%s | %d\n", position8, position8.getBitBoardIndex());
    }

    @Test
    public void testKingMovement() {
        ChessBoard board = new ChessBoard();
        ChessPiece king = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        ChessPosition kingPosition = new ChessPosition(4,5);
        board.addPiece(kingPosition, king);

        System.out.println(board);
        System.out.println(king.pieceMoves(board, kingPosition));

        board.addPiece(new ChessPosition(35), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        board.addPiece(new ChessPosition(21), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));

        System.out.println(board);
        System.out.println(king.pieceMoves(board, kingPosition));
    }

    @Test
    public void testKnightMovement() {
        ChessBoard board = new ChessBoard();
        ChessPiece knight = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        ChessPosition knightPosition = new ChessPosition(4, 5);
        board.addPiece(knightPosition, knight);

        System.out.println(board);
        System.out.println(knight.pieceMoves(board,knightPosition));
    }
}