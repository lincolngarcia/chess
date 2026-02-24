package passoff;

import chess.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

public class PersonalTests {
    @Test
    @DisplayName("Test Game Creation")
    public void GameCreation() {
        ChessGame game = new ChessGame();
        System.out.println(game);

        System.out.println("reset board:\n");
        game.getBoard().resetBoard();
        System.out.println(game);

    }

    @Test
    @DisplayName("Valid Movements")
    public void ValidMovements() {
        ChessGame game = new ChessGame();
        ;
        game.getBoard().resetBoard();

        Collection<ChessMove> moves = game.validMoves(new ChessPosition(2, 5));

        System.out.println(moves);
    }

    @Test
    @DisplayName("Test Game Movement")
    public void Movement() throws InvalidMoveException {
        ChessGame game = new ChessGame();
        ;
        game.getBoard().resetBoard();

        System.out.println(game);

        ArrayList<ChessMove> moves = new ArrayList<>(game.validMoves(new ChessPosition(2, 5)));

        System.out.println(moves);

        game.makeMove(moves.get(1));
        System.out.println(game);
    }

    @Test
    @DisplayName("Test Movement Exceptions")
    public void MoreMovement_1() {
        ChessGame game = new ChessGame();
        ;
        game.getBoard().addPiece(
                new ChessPosition(8, 2),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING)
        );

        game.getBoard().addPiece(
                new ChessPosition(7, 2),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN)
        );

        game.getBoard().addPiece(
                new ChessPosition(2, 7),
                new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN)
        );

        game.getBoard().addPiece(
                new ChessPosition(1, 7),
                new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING)
        );

        System.out.println(game);

        System.out.println(game.validMoves(new ChessPosition(2, 7)));
    }

    @Test
    @DisplayName("Test Movement Exceptions (pin)")
    public void MoreMovement_2() {
        ChessGame game = new ChessGame();
        ;

        game.getBoard().addPiece(
                new ChessPosition(5, 1),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING)
        );

        game.getBoard().addPiece(
                new ChessPosition(5, 2),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK)
        );

        game.getBoard().addPiece(
                new ChessPosition(5, 6),
                new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK)
        );

        game.getBoard().addPiece(
                new ChessPosition(5, 8),
                new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING)
        );


        System.out.println(game);

        System.out.println(game.validMoves(new ChessPosition(5, 6)));
    }

    @Test
    @DisplayName("Test Movement Exceptions (trapped)")
    public void MoreMovement_3() {
        ChessGame game = new ChessGame();

        game.getBoard().addPiece(
                new ChessPosition(8, 1),
                new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING)
        );

        game.getBoard().addPiece(
                new ChessPosition(8, 8),
                new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN)
        );

        game.getBoard().addPiece(
                new ChessPosition(4, 4),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK)
        );

        game.getBoard().addPiece(
                new ChessPosition(2, 2),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING)
        );


        System.out.println(game);

        System.out.println(game.validMoves(new ChessPosition(4, 4)));
    }


    @Test
    @DisplayName("Pinned kin causes stalemate")
    public void MoreMovement_4() {
        ChessGame game = new ChessGame();

        game.setBoard(new ChessBoard());

        game.getBoard().addPiece(
                new ChessPosition(8, 1),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING)
        );

        game.getBoard().addPiece(
                new ChessPosition(7, 8),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK)
        );

        game.getBoard().addPiece(
                new ChessPosition(5, 5),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN)
        );

        game.getBoard().addPiece(
                new ChessPosition(4, 4),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT)
        );

        game.getBoard().addPiece(
                new ChessPosition(4, 7),
                new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING)
        );

        game.getBoard().addPiece(
                new ChessPosition(1, 6),
                new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP)
        );


        System.out.println(game);
        System.out.println("WHITE: " + game.isInStalemate(ChessGame.TeamColor.WHITE));
        System.out.println("BLACK: " + game.isInStalemate(ChessGame.TeamColor.BLACK));

    }

    @Test
    @DisplayName("Test enums aren't copied by reference")
    public void enumTest() {
       ChessGame game = new ChessGame();
       ChessGame secondGame = new ChessGame(game);
       secondGame.setTeamTurn(ChessGame.TeamColor.BLACK);
       System.out.println(game);
        System.out.println(secondGame);
    }

}
