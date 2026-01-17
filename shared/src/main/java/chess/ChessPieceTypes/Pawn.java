package chess.ChessPieceTypes;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class Pawn extends ChessPieceType {
    public Pawn() {
        super();
    }

    @Override
    public Collection<ChessMove> getPieceMoves(ChessBoard board, ChessPosition position, ChessPiece piece) {
        // Set up global variables
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        int currentBitBoardIndex = position.getBitBoardIndex();
        int maxDirectionalMovement;

        // Determine if the pawn can jump
        if (position.getRow() == 2 || position.getRow() == 7) {
            maxDirectionalMovement = 2;
        } else {
            maxDirectionalMovement = 1;
        }

        // Set up the moves the pawn can travel in
        Collection<ChessMove.BoardMovement> movementDirections = new ArrayList<>();

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            movementDirections.add(ChessMove.BoardMovement.UP);

            int upper_left_attack_index = currentBitBoardIndex + ChessMove.BoardMovement.UP_LEFT.value();
            int upper_right_attack_index = currentBitBoardIndex + ChessMove.BoardMovement.UP_RIGHT.value();

            ChessPiece upper_left_piece = null;
            if (moveExists(position, new ChessPosition(upper_left_attack_index), ChessMove.BoardMovement.UP_LEFT)) {
                upper_left_piece = board.getPiece(upper_left_attack_index);
            }

            ChessPiece upper_right_piece = null;
            if (moveExists(position, new ChessPosition(upper_right_attack_index), ChessMove.BoardMovement.UP_RIGHT)) {
                upper_right_piece = board.getPiece(upper_right_attack_index);
            }

            // Add diagonal capturing straight to the pieceMoves
            if (upper_left_piece != null) {
                if (upper_left_piece.getTeamColor() != piece.getTeamColor()) {
                    ChessPosition endPosition = new ChessPosition(upper_left_attack_index);
                    if (endPosition.getRow() == 8) {
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.QUEEN));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.ROOK));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.BISHOP));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.KNIGHT));
                    }else{
                        pieceMoves.add(new ChessMove(position, ChessMove.BoardMovement.UP_LEFT.value()));
                    }
                }
            }

            if (upper_right_piece != null) {
                if (upper_right_piece.getTeamColor() != piece.getTeamColor()) {
                    ChessPosition endPosition = new ChessPosition(upper_right_attack_index);
                    if (endPosition.getRow() == 8) {
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.QUEEN));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.ROOK));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.BISHOP));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.KNIGHT));
                    }else{
                        pieceMoves.add(new ChessMove(position, ChessMove.BoardMovement.UP_RIGHT.value()));
                    }
                }
            }
        }

        if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            movementDirections.add(ChessMove.BoardMovement.DOWN);

            int lower_left_attack_index = currentBitBoardIndex + ChessMove.BoardMovement.DOWN_LEFT.value();
            int lower_right_attack_index = currentBitBoardIndex + ChessMove.BoardMovement.DOWN_RIGHT.value();

            ChessPiece lower_left_piece = null;
            if (moveExists(position, new ChessPosition(lower_left_attack_index), ChessMove.BoardMovement.DOWN_LEFT)) {
                lower_left_piece = board.getPiece(lower_left_attack_index);
            }

            ChessPiece lower_right_piece = null;
            if (moveExists(position, new ChessPosition(lower_right_attack_index), ChessMove.BoardMovement.DOWN_RIGHT)) {
                lower_right_piece = board.getPiece(lower_right_attack_index);
            }

            // Add diagonal capturing straight to the pieceMoves
            if (lower_left_piece != null) {
                if (lower_left_piece.getTeamColor() != piece.getTeamColor()) {
                    ChessPosition endPosition = new ChessPosition(lower_left_attack_index);
                    if (endPosition.getRow() == 1) {
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.QUEEN));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.ROOK));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.BISHOP));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.KNIGHT));
                    }else{
                        pieceMoves.add(new ChessMove(position, ChessMove.BoardMovement.DOWN_LEFT.value()));
                    }
                }
            }

            if (lower_right_piece != null) {
                if (lower_right_piece.getTeamColor() != piece.getTeamColor()) {
                    ChessPosition endPosition = new ChessPosition(lower_right_attack_index);
                    if (endPosition.getRow() == 1) {
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.QUEEN));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.ROOK));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.BISHOP));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.KNIGHT));
                    }else{
                        pieceMoves.add(new ChessMove(position, ChessMove.BoardMovement.DOWN_RIGHT.value()));
                    }
                }
            }
        }

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
                // Pawns can't attack forward
                if (targetedPiece == null) {
                    // add move to pieceMoves
                    if (endPosition.getRow() == 1 || endPosition.getRow() == 8) {
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.QUEEN));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.ROOK));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.BISHOP));
                        pieceMoves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.KNIGHT));
                        break;
                    } else {
                        pieceMoves.add(new ChessMove(position, offset));
                    }
                } else {
                    break;
                }
            }
        }

        return pieceMoves;
    }
}
