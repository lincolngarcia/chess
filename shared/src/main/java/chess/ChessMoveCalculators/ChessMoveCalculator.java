package chess.ChessMoveCalculators;

import chess.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class ChessMoveCalculator {
    @Override
    public String toString() {
        Collection<ChessMove> moves = this.getPieceMoves();
        Collection<ChessPosition> endPositions = new ArrayList<>();
        for (ChessMove move : moves) {
            endPositions.add(move.getEndPosition());
        }

        StringBuilder board = new StringBuilder();
        board.append("   A B C D E F G H\n");
        for (int row = 7; row >= 0; row--) {
            board.append(row + 1);
            board.append(" |");
            for (int col = 0; col < 8; col++) {

                String characterCode = ".";
                ChessPosition position = new ChessPosition(row + 1, col + 1);
                ChessPiece targetCell = this.board.getPiece(position);

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
                if (endPositions.contains(position)) board.append("\033[41m");
                board.append(characterCode);
                if (endPositions.contains(position)) board.append("\033[0m");
                board.append("|");
            }
            board.append(" ");
            board.append(row + 1);
            board.append("\n");
        }
        board.append("   A B C D E F G H\n");
        return board.toString();
    }

    ChessDirection[] movementDirections;
    int movementDistance;
    ChessBoard board;
    ChessPosition startPosition;

    public ChessMoveCalculator(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.startPosition = position;
    }

    abstract void setMovementDirections();

    abstract void setMovementDistance();

    public Collection<ChessMove> getPieceMoves() {
        this.setMovementDirections();
        this.setMovementDistance();

        ArrayList<ChessMove> pieceMoves = new ArrayList<>();

        for (ChessDirection direction : this.movementDirections) {
            for (int distance = 1; distance <= this.movementDistance; distance++) {
                int endIndex = this.startPosition.getBitboardIndex() + (distance * direction.value());

                ChessPosition endPosition = new ChessPosition(endIndex);
                ChessMove movement = new ChessMove(this.startPosition, endPosition);

                // Check if the move exists on the board
                if (isInvalidMove(movement, direction)) {
                    break;
                }

                // Check if the selected cell has a piece
                ChessPiece currentPiece = this.board.getPiece(this.startPosition);
                ChessPiece targetedPiece = this.board.getPiece(endPosition);
                if (targetedPiece != null) {
                    if (targetedPiece.getTeamColor() != currentPiece.getTeamColor()) {
                        pieceMoves.add(movement);
                    }
                    break;
                } else {
                    pieceMoves.add(movement);
                }

            }
        }
        return pieceMoves;
    }

    protected boolean isInvalidMove(ChessMove move, ChessDirection directionMoved) {
        // Move is invalid when index < 0 or greater than 63
        // Move is invalid when startCol > endCol when moving left
        // Move is invalid when startCol > endCol when moving right

        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        int endIndex = endPosition.getBitboardIndex();

        if (endIndex < 0 || endIndex >= 64) {
            return true;
        }

        switch (directionMoved) {
            case ChessDirection.LEFT_UP,
                 ChessDirection.LEFT,
                 ChessDirection.LEFT_DOWN,

                 ChessDirection.LEFT_UP_JUMP,
                 ChessDirection.LEFT_DOWN_JUMP,
                 ChessDirection.UP_LEFT_JUMP,
                 ChessDirection.DOWN_LEFT_JUMP:
                if (startPosition.getColumn() < endPosition.getColumn()) {
                    return true;
                }
                break;

            case ChessDirection.RIGHT_UP,
                 ChessDirection.RIGHT,
                 ChessDirection.RIGHT_DOWN,

                 ChessDirection.RIGHT_UP_JUMP,
                 ChessDirection.RIGHT_DOWN_JUMP,
                 ChessDirection.UP_RIGHT_JUMP,
                 ChessDirection.DOWN_RIGHT_JUMP:
                if (startPosition.getColumn() > endPosition.getColumn()) {
                    return true;
                }
        }

        return false;
    }
}