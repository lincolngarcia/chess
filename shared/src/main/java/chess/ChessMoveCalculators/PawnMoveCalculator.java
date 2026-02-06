package chess.ChessMoveCalculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator extends ChessMoveCalculator {
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

    void setMovementDistance(ChessPosition position, ChessGame.TeamColor color) {
        int startRow = color == ChessGame.TeamColor.WHITE ? 2 : 7;
        if (position.getRow() == startRow) {
            this.movementDistance = 2;
        } else {
            this.movementDistance = 1;
        }
    }

    @Override
    public Collection<ChessMove> getPieceMoves() {
        ChessGame.TeamColor color = this.board.getPiece(this.startPosition).getTeamColor();
        this.setMovementDirections();
        this.setMovementDistance(startPosition, color);

        ArrayList<ChessMove> pieceMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);

        // Forward movement
        if (piece == null) {
            return null;
        }

        for (int distance = 1; distance <= this.movementDistance; distance++) {
            ChessDirection trueMovementDirection = color == ChessGame.TeamColor.WHITE ? ChessDirection.UP : ChessDirection.DOWN;
            int endIndex = startPosition.getBitboardIndex() + (distance * trueMovementDirection.value());
            ChessPosition endPosition = new ChessPosition(endIndex);
            ChessMove movement = new ChessMove(startPosition, endPosition);

            // Check if the move exists on the board
            if (isInvalidMove(movement, trueMovementDirection)) {
                break;
            }

            if (board.getPiece(endPosition) == null) {
                this.addPieceMove(pieceMoves, movement, color);
            } else {
                break;
            }


        }

        // Check diagonal capture
        if (color == ChessGame.TeamColor.WHITE) {
            int right_attack_offset = ChessDirection.RIGHT_UP.value() + startPosition.getBitboardIndex();
            ChessPosition right_attack_pos = new ChessPosition(right_attack_offset);

            if (!isInvalidMove(new ChessMove(startPosition, right_attack_pos), ChessDirection.RIGHT_UP)) {
                ChessPiece right_attack = board.getPiece(right_attack_pos);
                if (right_attack != null && right_attack.getTeamColor() != ChessGame.TeamColor.WHITE) {
                    this.addPieceMove(pieceMoves, new ChessMove(startPosition, right_attack_pos), ChessGame.TeamColor.WHITE);
                }
            }

            int left_attack_offset = ChessDirection.LEFT_UP.value() + startPosition.getBitboardIndex();
            ChessPosition left_attack_pos = new ChessPosition(left_attack_offset);

            if (!isInvalidMove(new ChessMove(startPosition, left_attack_pos), ChessDirection.LEFT_UP)) {
                ChessPiece left_attack = board.getPiece(left_attack_pos);
                if (left_attack != null && left_attack.getTeamColor() != ChessGame.TeamColor.WHITE) {
                    this.addPieceMove(pieceMoves, new ChessMove(startPosition, left_attack_pos), ChessGame.TeamColor.WHITE);
                }
            }
        } else {
            int right_attack_offset = ChessDirection.LEFT_DOWN.value() + startPosition.getBitboardIndex();
            ChessPosition right_attack_pos = new ChessPosition(right_attack_offset);

            if (!isInvalidMove(new ChessMove(startPosition, right_attack_pos), ChessDirection.LEFT_DOWN)) {
                ChessPiece right_attack = board.getPiece(right_attack_pos);
                if (right_attack != null && right_attack.getTeamColor() != ChessGame.TeamColor.BLACK) {
                    this.addPieceMove(pieceMoves, new ChessMove(startPosition, right_attack_pos), ChessGame.TeamColor.BLACK);
                }
            }

            int left_attack_offset = ChessDirection.RIGHT_DOWN.value() + startPosition.getBitboardIndex();
            ChessPosition left_attack_pos = new ChessPosition(left_attack_offset);

            if (!isInvalidMove(new ChessMove(startPosition, left_attack_pos), ChessDirection.RIGHT_DOWN)) {
                ChessPiece left_attack = board.getPiece(left_attack_pos);
                if (left_attack != null && left_attack.getTeamColor() != ChessGame.TeamColor.BLACK) {
                    this.addPieceMove(pieceMoves, new ChessMove(startPosition, left_attack_pos), ChessGame.TeamColor.BLACK);
                }
            }
        }

        return pieceMoves;
    }

    private void addPieceMove(Collection<ChessMove> pieceMoves, ChessMove move, ChessGame.TeamColor color) {
        int promotionRow = color == ChessGame.TeamColor.WHITE ? 8 : 1;
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
