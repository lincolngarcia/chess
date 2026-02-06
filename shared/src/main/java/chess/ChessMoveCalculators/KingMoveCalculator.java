package chess.ChessMoveCalculators;

import chess.ChessBoard;
import chess.ChessDirection;
import chess.ChessPosition;

public class KingMoveCalculator extends ChessMoveCalculator{
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
                ChessDirection.RIGHT_DOWN
        };
    }

    @Override
    void setMovementDistance() {
        this.movementDistance = 1;
    }

    public KingMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
}
