package chess.ChessMoveCalculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SuperQueenMoveCalculator extends ChessMoveCalculator {
    @Override
    void setMovementDirections() {
        this.movementDirections = new ChessDirection[]{
                ChessDirection.LEFT_UP,
                ChessDirection.UP,
                ChessDirection.RIGHT_UP,
                ChessDirection.LEFT,
                ChessDirection.RIGHT,
                ChessDirection.LEFT_DOWN,
                ChessDirection.DOWN,
                ChessDirection.RIGHT_DOWN,

                ChessDirection.UP_LEFT_JUMP,
                ChessDirection.UP_RIGHT_JUMP,
                ChessDirection.LEFT_UP_JUMP,
                ChessDirection.RIGHT_UP_JUMP,
                ChessDirection.LEFT_DOWN_JUMP,
                ChessDirection.RIGHT_DOWN_JUMP,
                ChessDirection.DOWN_LEFT_JUMP,
                ChessDirection.DOWN_RIGHT_JUMP
        };
    }

    @Override
    void setMovementDistance() {
        this.movementDistance = 7;
    }

    /**
     * Returns all pieces that can be targeted
     *
     * @return A collection of positions containing targeted pieces
     */
    public Collection<ChessPosition> getTargetedPieces() {
        this.setMovementDirections();
        this.setMovementDistance();

        ArrayList<ChessPosition> targetedPieces = new ArrayList<>();

        Collection<ChessDirection> jumpMoves = new ArrayList<>(List.of(
                ChessDirection.UP_LEFT_JUMP,
                ChessDirection.UP_RIGHT_JUMP,
                ChessDirection.LEFT_UP_JUMP,
                ChessDirection.RIGHT_UP_JUMP,
                ChessDirection.LEFT_DOWN_JUMP,
                ChessDirection.RIGHT_DOWN_JUMP,
                ChessDirection.DOWN_LEFT_JUMP,
                ChessDirection.DOWN_RIGHT_JUMP
        ));

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
                        targetedPieces.add(endPosition);
                    }
                    break;
                }
                // If ChessMove is in Knight List, break;
                if (jumpMoves.contains(direction)) break;
            }
        }
        return targetedPieces;
    }

    public SuperQueenMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
}
