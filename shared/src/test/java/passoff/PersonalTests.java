package passoff;

import chess.*;
import chess.ChessMoveCalculators.ChessMoveCalculator;
import chess.ChessMoveCalculators.QueenMoveCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PersonalTests {
    public List<ChessMove> getAllMoves(ChessGame game, ChessGame.TeamColor color) {
       List<ChessMove> allMoves = new ArrayList<>();
       Collection<ChessPosition> teamPositions = game.getBoard().getTeamPositions(color);
       for (ChessPosition teamPosition : teamPositions) {
           allMoves.addAll(game.validMoves(teamPosition));
       }

       return allMoves;
    }

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

        game.getBoard().resetBoard();

        Collection<ChessMove> moves = game.validMoves(new ChessPosition(2, 5));
    }

    @Test
    @DisplayName("Calculator toString method")
    public void toStringTests() {
        ChessGame game = new ChessGame();
        game.getBoard().resetBoard();

        try {
            int moveCount = 26;
            int SEED = 10298;
            for (int i = 0; i < moveCount; i++) {
                List<ChessMove> allMoves = this.getAllMoves(game, game.getTeamTurn());
                int index = Math.abs(allMoves.hashCode() * SEED) % allMoves.size();
                game.makeMove(allMoves.get(index));
            }
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }

        Collection<ChessPosition> blackPositions = game.getBoard().getTeamPositions(ChessGame.TeamColor.BLACK);
        ChessPosition blackQueenPOS = null;
        for (ChessPosition blackPosition : blackPositions) {
            if (game.getBoard().getPiece(blackPosition).getPieceType() == ChessPiece.PieceType.QUEEN) {
                blackQueenPOS = blackPosition;
                break;
            }
        }

        ChessMoveCalculator calculator = new QueenMoveCalculator(game.getBoard(), blackQueenPOS);

        Collection<ChessPosition> whitePositions = game.getBoard().getTeamPositions(ChessGame.TeamColor.WHITE);
        ChessPosition whiteQueenPOS = null;
        for (ChessPosition whitePosition : whitePositions) {
            if (game.getBoard().getPiece(whitePosition).getPieceType() == ChessPiece.PieceType.QUEEN) {
                whiteQueenPOS = whitePosition;
                break;
            }
        }

        ChessMoveCalculator whiteCalculator = new QueenMoveCalculator(game.getBoard(), whiteQueenPOS);

        System.out.println(calculator);
        System.out.println(whiteCalculator);

        System.out.println(game.getBoard().getHistory());
        game.getBoard().printHistory();
    }

    @Test
    @DisplayName("Test Game Movement")
    public void Movement() throws InvalidMoveException {
        ChessGame game = new ChessGame();
        ;
        game.getBoard().resetBoard();

        ArrayList<ChessMove> moves = new ArrayList<>(game.validMoves(new ChessPosition(2, 5)));

        game.makeMove(moves.get(1));
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
    }

    @Test
    @DisplayName("Test enums aren't copied by reference")
    public void enumTest() {
       ChessGame game = new ChessGame();
       ChessGame secondGame = new ChessGame(game);
       secondGame.setTeamTurn(ChessGame.TeamColor.BLACK);
    }

}
