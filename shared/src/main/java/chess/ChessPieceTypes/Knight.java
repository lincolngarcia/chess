package chess.ChessPieceTypes;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Knight extends ChessPieceType {
    public Knight() {
        super();
    }

    @Override
    public Collection<ChessMove> getPieceMoves(ChessBoard board, ChessPosition position, ChessPiece piece) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        int currentBitBoardIndex = position.getBitBoardIndex();
        int maxDirectionalMovement = 1;

        Collection<ChessMove.BoardMovement> movementDirections = new ArrayList<>(List.of(
                ChessMove.BoardMovement.UP_LEFT_JUMP,
                ChessMove.BoardMovement.UP_RIGHT_JUMP,
                ChessMove.BoardMovement.LEFT_UP_JUMP,
                ChessMove.BoardMovement.RIGHT_UP_JUMP,
                ChessMove.BoardMovement.DOWN_LEFT_JUMP,
                ChessMove.BoardMovement.DOWN_RIGHT_JUMP,
                ChessMove.BoardMovement.LEFT_DOWN_JUMP,
                ChessMove.BoardMovement.RIGHT_DOWN_JUMP
        ));

        // Loop through the move directions and see what moves are legal
        for (ChessMove.BoardMovement direction : movementDirections) {
            for (int distance = 0; distance < maxDirectionalMovement; distance++) {
                // Figure out the new cell
                int sign = Integer.signum(direction.value());
                int offset = Math.abs(direction.value()) * (distance + 1) * sign;
                int endPositionIndex = currentBitBoardIndex + offset;
                ChessPosition endPosition = new ChessPosition(endPositionIndex);

                if (!moveExists(position, endPosition, direction)) {
                    break;
                }

                // Figure out if the cell is a new piece, or an empty cell
                ChessPiece targetedPiece = board.getPiece(endPosition);

                if (targetedPiece == null) {
                    pieceMoves.add(new ChessMove(position, offset));
                    continue;
                }

                if (targetedPiece.getTeamColor() != piece.getTeamColor()) {
                    pieceMoves.add(new ChessMove(position, offset));
                    break;
                } else {
                    break;
                }
            }
        }

        return pieceMoves;
    }

}
