package chess.movecalculators;

import chess.*;
import chess.converter.ChessFunctions;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator extends ChessMoveCalculator {
    protected int directionInverter;

    // Standard Overrides
    @Override
    void setMovementDirections() {
        this.movementDirections = new ChessDirection[]{ChessDirection.UP};
    }

    @Override
    void setMovementDistance() {
        // Figure out what the color's starting row is
        if (ChessFunctions.getPawnStartRow(this.getTeamColor()) == position.getRow()) {
            this.movementDistance = 2;
        } else {
            this.movementDistance = 1;
        }
    }

    // Constructors
    public PawnMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
        this.directionInverter = this.getTeamColor().value();
    }

    @Override
    public Collection<ChessMove> getPieceMoves() {
        // Correct directions
        ChessDirection forwardDirection = this.directionInverter == 1 ? ChessDirection.UP : ChessDirection.DOWN;
        ChessDirection leftAttackDirection = this.directionInverter == 1 ? ChessDirection.LEFT_UP : ChessDirection.RIGHT_DOWN;
        ChessDirection rightAttackDirection = this.directionInverter == 1 ? ChessDirection.RIGHT_UP : ChessDirection.LEFT_DOWN;

        ArrayList<ChessMove> pieceMoves = new ArrayList<>();

        // front-forward
        int startPositionIndex = this.getPosition().getBitboardIndex();
        int forwardOffset = ChessDirection.UP.value() * directionInverter;
        int forwardCellIndex = startPositionIndex + forwardOffset;
        ChessPosition forwardCell = new ChessPosition(forwardCellIndex);
        ChessMove forwardMove = new ChessMove(this.getPosition(), forwardCell);
        boolean isForwardBlocked = true;

        if (!isInvalidMove(forwardMove, forwardDirection)) {
            isForwardBlocked = this.board.getPiece(forwardCell) != null;
            if (!isForwardBlocked) {
                this.addPieceMove(pieceMoves, new ChessMove(this.getPosition(), forwardCell));
            }
        }

        // front-forward-jump
        if (!isForwardBlocked && movementDistance == 2) {
            this.movementDistance = 1;
            int forwardJumpOffset = forwardOffset * 2;
            int forwardJumpCellIndex = startPositionIndex + forwardJumpOffset;
            ChessPosition forwardJumpCell = new ChessPosition(forwardJumpCellIndex);
            ChessMove forwardJumpMove = new ChessMove(this.getPosition(), forwardJumpCell);

            if (!isInvalidMove(forwardJumpMove, forwardDirection)) {
                boolean isForwardJumpBlocked = this.board.getPiece(forwardJumpCell) != null;
                if (!isForwardJumpBlocked) {
                    this.addPieceMove(pieceMoves, forwardJumpMove);
                }
            }
        }

        // left attack
        int leftAttackOffset = ChessDirection.LEFT_UP.value() * directionInverter;
        int leftAttackCellIndex = startPositionIndex + leftAttackOffset;
        ChessPosition leftAttackCell = new ChessPosition(leftAttackCellIndex);
        ChessMove leftAttack = new ChessMove(this.getPosition(), leftAttackCell);
        if (!this.isInvalidMove(leftAttack, leftAttackDirection)) {
            ChessPiece targetedPiece = this.board.getPiece(leftAttackCell);
            if (targetedPiece != null && targetedPiece.getTeamColor() != this.getTeamColor()) {
                this.addPieceMove(pieceMoves, leftAttack);
            } else if (leftAttackCell.equals(this.getBoard().getEnPassantSquare())) {
                this.addPieceMove(pieceMoves, leftAttack);
            }
        }

        // right attack
        int rightAttackOffset = ChessDirection.RIGHT_UP.value() * directionInverter;
        int rightAttackCellIndex = startPositionIndex + rightAttackOffset;
        ChessPosition rightAttackCell = new ChessPosition(rightAttackCellIndex);
        ChessMove rightAttack = new ChessMove(this.getPosition(), rightAttackCell);
        if (!this.isInvalidMove(rightAttack, rightAttackDirection)) {
            ChessPiece targetedPiece = this.board.getPiece(rightAttackCell);
            if (targetedPiece != null && targetedPiece.getTeamColor() != this.getTeamColor()) {
                this.addPieceMove(pieceMoves, rightAttack);
            } else if (rightAttackCell.equals(this.getBoard().getEnPassantSquare())) {
                this.addPieceMove(pieceMoves, rightAttack);
            }
        }

        return pieceMoves;
    }

    private void addPieceMove(Collection<ChessMove> pieceMoves, ChessMove move) {
        int promotionRow = ChessFunctions.getPromotionRow(this.getTeamColor());

        ChessPosition endPosition = move.getEndPosition();

        if (endPosition.getRow() == promotionRow) {
            pieceMoves.add(new ChessMove(this.getPosition(), endPosition, ChessPiece.PieceType.KNIGHT));
            pieceMoves.add(new ChessMove(this.getPosition(), endPosition, ChessPiece.PieceType.BISHOP));
            pieceMoves.add(new ChessMove(this.getPosition(), endPosition, ChessPiece.PieceType.ROOK));
            pieceMoves.add(new ChessMove(this.getPosition(), endPosition, ChessPiece.PieceType.QUEEN));
        } else {
            pieceMoves.add(move);
        }
    }
}
