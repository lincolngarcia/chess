package chess.ChessPieceTypes;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Rook extends ChessPieceType{
    public Rook() {super();}

    @Override
    public Collection<ChessMove> getPieceMoves(ChessBoard board, ChessPosition position, ChessPiece piece) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        int currentBitBoardIndex = position.getBitBoardIndex();
        int maxDirectionalMovement = 7;

        Collection<ChessMove.BoardMovement> movementDirections = new ArrayList<>(List.of(
                ChessMove.BoardMovement.UP,
                ChessMove.BoardMovement.LEFT,
                ChessMove.BoardMovement.RIGHT,
                ChessMove.BoardMovement.DOWN
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
