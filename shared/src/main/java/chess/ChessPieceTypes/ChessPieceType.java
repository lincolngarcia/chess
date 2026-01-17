package chess.ChessPieceTypes;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessPieceType {
    public ChessPieceType() {
        return;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> getPieceMoves(ChessBoard board, ChessPosition position, ChessPiece piece) {
        return new ArrayList<>();
    }

    boolean moveExists(ChessPosition position, ChessPosition endPosition, ChessMove.BoardMovement direction) {
        // See if that index exists on the board
        int endPositionIndex = endPosition.getBitBoardIndex();
        if (endPositionIndex < 0 || endPositionIndex >= 64) {
            return false;
        }

        // See if we jumped from one side of the board to another
        switch (direction) {
            // If moving left, target cell cannot have a higher column
            case UP_LEFT:
            case LEFT:
            case DOWN_LEFT:
            case UP_LEFT_JUMP:
            case LEFT_UP_JUMP:
            case LEFT_DOWN_JUMP:
            case DOWN_LEFT_JUMP:
                if (endPosition.getColumn() >= position.getColumn()) {
                    return false;
                }
                break;

            // If moving right, target cell cannot have a lower column
            case UP_RIGHT:
            case RIGHT:
            case DOWN_RIGHT:
            case UP_RIGHT_JUMP:
            case RIGHT_UP_JUMP:
            case RIGHT_DOWN_JUMP:
            case DOWN_RIGHT_JUMP:
                if (endPosition.getColumn() <= position.getColumn()) {
                    return false;
                }
                break;
        }

        return true;

    }

    public void printMoves(ChessBoard board, ChessPiece piece, ChessPosition position) {
        System.out.print("   A B C D E F G H\n");

        Collection<ChessMove> pieceMoves = this.getPieceMoves(board, position, piece);
        Collection<Integer> pieceHashes = new ArrayList<>();
        for (ChessMove pieceMove : pieceMoves) {
            pieceHashes.add(pieceMove.getEndPosition().hashCode());
        }

        for (int row = 7; row >= 0; row--) {
            System.out.print(row + 1);
            System.out.print(" |");
            for (int col = 0; col < 8; col++) {
                boolean printRed = false;
                ChessPosition currentPosition = new ChessPosition(row + 1, col + 1);
                if (pieceHashes.contains(currentPosition.hashCode())) printRed = true;

                String characterCode = ".";
                ChessPiece targetCell = board.getPiece(new ChessPosition(row + 1, col + 1));
                if (targetCell != null) {
                    characterCode = switch (targetCell.getPieceType()) {
                        case KING -> "K";
                        case QUEEN -> "Q";
                        case ROOK -> "R";
                        case BISHOP -> "B";
                        case KNIGHT -> "N";
                        case PAWN -> "P";
                    };
                    // White is UpperCase
                    if (targetCell.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        characterCode = characterCode.toLowerCase();
                    }
                }

                if (printRed) System.out.print("\033[41m");
                System.out.print(characterCode);
                if (printRed) System.out.print("\033[0m");

                System.out.print("|");
            }
            System.out.print(" ");
            System.out.print(row + 1);
            System.out.print("\n");
        }
        System.out.print("   A B C D E F G H\n");
    }
}
