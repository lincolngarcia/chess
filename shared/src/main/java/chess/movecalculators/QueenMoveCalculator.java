package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessPosition;

public class QueenMoveCalculator extends ChessMoveCalculator{
    // Standard Overrides
    @Override
    void setMovementDirections() {
        this.movementDirections = ALL_DIRECTIONS;
    }

    @Override
    void setMovementDistance() {
        this.movementDistance = 7;
    }

    // Constructors
    public QueenMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
}
