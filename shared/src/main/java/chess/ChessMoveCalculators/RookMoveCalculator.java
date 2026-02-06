package chess.ChessMoveCalculators;

import chess.ChessBoard;
import chess.ChessDirection;
import chess.ChessPosition;

public class RookMoveCalculator extends ChessMoveCalculator{
    @Override
    void setMovementDirections() {
        this.movementDirections = new ChessDirection[]{
                ChessDirection.UP,
                ChessDirection.LEFT,
                ChessDirection.RIGHT,
                ChessDirection.DOWN,
        };
    }

    @Override
    void setMovementDistance() {
        this.movementDistance = 7;
    }

    public RookMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
}
