package chess.ChessMoveCalculators;

import chess.*;

public class SuperQueenMoveCalculator extends ChessMoveCalculator {
    // Standard Overrides
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

    // Constructors
    public SuperQueenMoveCalculator(ChessBoard board, ChessPosition position, ChessGame.TeamColor teamColor) {
        super(board, position, teamColor);
    }
}
