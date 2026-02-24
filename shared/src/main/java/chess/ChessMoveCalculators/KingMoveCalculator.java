package chess.ChessMoveCalculators;

import chess.*;

import java.util.Collection;

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

    @Override
    public Collection<ChessMove> getPieceMoves() {
        // Get normal moves
        Collection<ChessMove> pieceMoves = super.getPieceMoves();

        // Store variables
        ChessPiece piece = this.board.getPiece(this.startPosition);
        ChessGame.TeamColor pieceColor = piece.getTeamColor();
        int row = pieceColor == ChessGame.TeamColor.WHITE ? 1 : 8;

        // Check for castling
        int[] rookColumns = new int[] {1, 8};
        for (int i = 0; i < 2; i++) {
            int rookCol = rookColumns[i];
            int kingEndCol = rookCol == 1 ? 3 : 7;

            if (canCastle(rookCol)) {
                ChessPosition endPosition = new ChessPosition(row, kingEndCol);
                ChessMove move = new ChessMove(this.startPosition, endPosition);
                pieceMoves.add(move);
            }
        }

        return pieceMoves;
    }

    public boolean canCastle(int rookCol) {
        ChessPiece piece = this.board.getPiece(this.startPosition);
        if (piece == null) return false;
        if (piece.getPieceType() != ChessPiece.PieceType.KING) return false;

        ChessGame.TeamColor pieceColor = piece.getTeamColor();

        // Check if the king has moved
        if (this.getBoard().hasKingMoved(pieceColor)) return false;

        // Check if the rook has moved
        if (this.getBoard().hasRookMoved(pieceColor, rookCol)) return false;

        // Check that the rook exists
        int row = pieceColor == ChessGame.TeamColor.WHITE ? 1 : 8;
        ChessPosition rookPosition = new ChessPosition(row, rookCol);
        ChessPiece rook = this.getBoard().getPiece(rookPosition);
        if (rook == null) return false;
        if (rook.getPieceType() != ChessPiece.PieceType.ROOK) return false;

        // Check that pieces aren't blocking the path
        int[] columnsToCheck;
        if (rookCol == 1) {
            columnsToCheck = new int[] {2, 3, 4};
        }else{
            columnsToCheck = new int[] {6, 7};
        }

        for (int col : columnsToCheck) {
            ChessPosition position = new ChessPosition(row, col);
            if (this.getBoard().getPiece(position) != null) return false;
        }

        return true;
    }
}
