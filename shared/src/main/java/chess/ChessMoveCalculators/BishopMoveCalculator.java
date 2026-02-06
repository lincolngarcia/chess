package chess.ChessMoveCalculators;

import chess.ChessBoard;
import chess.ChessDirection;
import chess.ChessPosition;

public class BishopMoveCalculator extends ChessMoveCalculator {
    @Override
    void setMovementDirections() {
        this.movementDirections = new ChessDirection[]{
                ChessDirection.LEFT_UP,
                ChessDirection.RIGHT_UP,
                ChessDirection.LEFT_DOWN,
                ChessDirection.RIGHT_DOWN
        };
    }

    @Override
    void setMovementDistance() {
        this.movementDistance = 7;
    }

    public BishopMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
}