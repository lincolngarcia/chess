package chess.ChessMoveCalculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator extends ChessMoveCalculator {
    protected int colorInverter = 0;

    @Override
    void setMovementDirections() {
        this.movementDirections = new ChessDirection[]{ChessDirection.UP};
    }

    @Override
    void setMovementDistance() {
        this.movementDistance = 0;
    }

    public PawnMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    void setMovementDistance(ChessPosition position) {
        // Figure out what the color's starting row is
        if (4.5 - (2.5 * this.colorInverter) == position.getRow()) {
            this.movementDistance = 2;
        } else {
            this.movementDistance = 1;
        }
    }

    @Override
    public Collection<ChessMove> getPieceMoves() {
        ChessPiece piece = board.getPiece(startPosition);
        ChessGame.TeamColor pieceColor = piece.getTeamColor();
        this.colorInverter = pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;

        ChessDirection forwardDirection = this.colorInverter == 1 ? ChessDirection.UP : ChessDirection.DOWN;
        ChessDirection leftAttackDirection = this.colorInverter == 1 ? ChessDirection.LEFT_UP : ChessDirection.RIGHT_DOWN;
        ChessDirection rightAttackDirection = this.colorInverter == 1 ? ChessDirection.RIGHT_UP : ChessDirection.LEFT_DOWN;

        this.setMovementDirections();
        this.setMovementDistance(startPosition);

        ArrayList<ChessMove> pieceMoves = new ArrayList<>();

        // front-forward
        int startPositionIndex = this.startPosition.getBitboardIndex();
        int forwardOffset = ChessDirection.UP.value() * colorInverter;
        int forwardCellIndex = startPositionIndex + forwardOffset;
        ChessPosition forwardCell = new ChessPosition(forwardCellIndex);
        ChessMove forwardMove = new ChessMove(this.startPosition, forwardCell);
        boolean isForwardBlocked = true;

        if (!isInvalidMove(forwardMove, forwardDirection)) {
            isForwardBlocked = this.board.getPiece(forwardCell) != null;
            if (!isForwardBlocked) {
                this.addPieceMove(pieceMoves, new ChessMove(startPosition, forwardCell));
            }
        }

        // front-forward-jump
        if (!isForwardBlocked && movementDistance == 2) {
            this.movementDistance = 1;
            int forwardJumpOffset = forwardOffset * 2;
            int forwardJumpCellIndex = startPositionIndex + forwardJumpOffset;
            ChessPosition forwardJumpCell = new ChessPosition(forwardJumpCellIndex);
            ChessMove forwardJumpMove = new ChessMove(this.startPosition, forwardJumpCell);

            if (!isInvalidMove(forwardJumpMove, forwardDirection)) {
                boolean isForwardJumpBlocked = this.board.getPiece(forwardJumpCell) != null;
                if (!isForwardJumpBlocked) {
                    this.addPieceMove(pieceMoves, forwardJumpMove);
                }
            }
        }

        // left attack
        int leftAttackOffset = ChessDirection.LEFT_UP.value() * colorInverter;
        int leftAttackCellIndex = startPositionIndex + leftAttackOffset;
        ChessPosition leftAttackCell = new ChessPosition(leftAttackCellIndex);
        ChessMove leftAttack = new ChessMove(this.startPosition, leftAttackCell);
        if (!isInvalidMove(leftAttack, leftAttackDirection)) {
            ChessPiece targetedPiece = this.board.getPiece(leftAttackCell);
            if (targetedPiece != null && targetedPiece.getTeamColor() != pieceColor) {
                this.addPieceMove(pieceMoves, leftAttack);
            }
        }

        // right attack
        int rightAttackOffset = ChessDirection.RIGHT_UP.value() * colorInverter;
        int rightAttackCellIndex = startPositionIndex + rightAttackOffset;
        ChessPosition rightAttackCell = new ChessPosition(rightAttackCellIndex);
        ChessMove rightAttack = new ChessMove(this.startPosition, rightAttackCell);
        if (!isInvalidMove(rightAttack, rightAttackDirection)) {
            ChessPiece targetedPiece = this.board.getPiece(rightAttackCell);
            if (targetedPiece != null && targetedPiece.getTeamColor() != pieceColor) {
                this.addPieceMove(pieceMoves, rightAttack);
            }
        }

        return pieceMoves;
    }

    private void addPieceMove(Collection<ChessMove> pieceMoves, ChessMove move) {
        int promotionRow = (int) (4.5 + (3.5 * this.colorInverter));

        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        if (endPosition.getRow() == promotionRow) {
            pieceMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.KNIGHT));
            pieceMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.BISHOP));
            pieceMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.ROOK));
            pieceMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.QUEEN));
        } else {
            pieceMoves.add(move);
        }
    }
}
