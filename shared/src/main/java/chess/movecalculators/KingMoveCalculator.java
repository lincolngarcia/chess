package chess.movecalculators;

import chess.*;
import chess.converter.ChessFunctions;

import java.util.Collection;

public class KingMoveCalculator extends ChessMoveCalculator {
    // Standard Overrides
    @Override
    void setMovementDirections() {
        this.movementDirections = allDirections;
    }

    @Override
    void setMovementDistance() {
        this.movementDistance = 1;
    }

    // Constructors
    public KingMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    // Logic Heavy Functions

    /**
     * Determines if the king of this.teamColor can
     * castle towards rook of @param rookCol
     *
     * @param rookCol the starting column of the rook
     * @return boolean if the move is possible (ignores checks)
     */
    public boolean canCastle(int rookCol) {
        // King must exist
        if (this.piece == null) {
            return false;
        }
        if (piece.getPieceType() != ChessPiece.PieceType.KING) {
            return false;
        }

        // King must be in  it's starting location
        int startingRow = ChessFunctions.getStartRow(this.getTeamColor());
        int startingCol = 5;
        if (this.getPosition().getRow() != startingRow) {
            return false;
        }
        if (this.getPosition().getColumn() != startingCol) {
            return false;
        }

        // Check if the king has moved
        if (this.getBoard().hasKingMoved(this.getTeamColor())) {
            return false;
        }

        // Check if the rook has moved
        if (this.getBoard().hasRookMoved(this.getTeamColor(), rookCol)) {
            return false;
        }

        // Check that the rook exists
        ChessPosition rookPosition = new ChessPosition(startingRow, rookCol);
        ChessPiece rook = this.getBoard().getPiece(rookPosition);
        if (rook == null) {
            return false;
        }
        if (rook.getPieceType() != ChessPiece.PieceType.ROOK) {
            return false;
        }

        // Check that pieces aren't blocking the path
        int[] columnsToCheck;
        if (rookCol == 1) {
            columnsToCheck = new int[]{2, 3, 4};
        } else {
            columnsToCheck = new int[]{6, 7};
        }

        for (int col : columnsToCheck) {
            ChessPosition position = new ChessPosition(startingRow, col);
            if (this.getBoard().getPiece(position) != null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Collection<ChessMove> getPieceMoves() {
        // Get normal moves
        Collection<ChessMove> pieceMoves = super.getPieceMoves();

        // Store variables
        ChessPiece piece = this.board.getPiece(this.getPosition());
        ChessGame.TeamColor pieceColor = piece.getTeamColor();
        int startingRow = ChessFunctions.getStartRow(pieceColor);

        // Check for castling
        int[] rookColumns = new int[]{1, 8};
        for (int i = 0; i < 2; i++) {
            int rookCol = rookColumns[i];
            int kingEndCol = rookCol == 1 ? 3 : 7;

            if (canCastle(rookCol)) {
                ChessPosition endPosition = new ChessPosition(startingRow, kingEndCol);
                ChessMove move = new ChessMove(this.getPosition(), endPosition);
                pieceMoves.add(move);
            }
        }

        return pieceMoves;
    }
}
