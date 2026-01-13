package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    PieceType pieceType;
    ChessGame.TeamColor pieceColor;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceType == that.pieceType && pieceColor == that.pieceColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceType, pieceColor);
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceType = type;
        this.pieceColor = pieceColor;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition currentPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();

        int currentBitBoardIndex = currentPosition.getBitBoardIndex();

        // Set the Max Directional Movement for Each Piece
        int maxDirectionalMovement = 0; // Default distance for most pieces
        switch (this.pieceType) {
            case KING:
            case KNIGHT:
                maxDirectionalMovement = 1;
                break;

            case QUEEN:
            case ROOK:
            case BISHOP:
                maxDirectionalMovement = 7;
                break;

            case PAWN:
                // If pawn is on starting rank, it's two, if not, 1
                if (currentPosition.getRow() == 2 || currentPosition.getRow() == 7) {
                    maxDirectionalMovement = 2;
                } else {
                    maxDirectionalMovement = 1;
                }
        }

        // Get the movement types of each piece
        ArrayList<ChessMove.BoardMovement> movementDirections = new ArrayList<>();
        switch (this.pieceType) {
            case KING:
            case QUEEN:
                movementDirections.add(ChessMove.BoardMovement.UP_LEFT);
                movementDirections.add(ChessMove.BoardMovement.UP);
                movementDirections.add(ChessMove.BoardMovement.UP_RIGHT);
                movementDirections.add(ChessMove.BoardMovement.LEFT);
                movementDirections.add(ChessMove.BoardMovement.RIGHT);
                movementDirections.add(ChessMove.BoardMovement.DOWN_LEFT);
                movementDirections.add(ChessMove.BoardMovement.DOWN);
                movementDirections.add(ChessMove.BoardMovement.DOWN_RIGHT);
                break;

            case ROOK:
                movementDirections.add(ChessMove.BoardMovement.UP);
                movementDirections.add(ChessMove.BoardMovement.LEFT);
                movementDirections.add(ChessMove.BoardMovement.RIGHT);
                movementDirections.add(ChessMove.BoardMovement.DOWN);
                break;

            case BISHOP:
                movementDirections.add(ChessMove.BoardMovement.UP_LEFT);
                movementDirections.add(ChessMove.BoardMovement.UP_RIGHT);
                movementDirections.add(ChessMove.BoardMovement.DOWN_LEFT);
                movementDirections.add(ChessMove.BoardMovement.DOWN_RIGHT);
                break;

            case KNIGHT:
                movementDirections.add(ChessMove.BoardMovement.UP_LEFT_JUMP);
                movementDirections.add(ChessMove.BoardMovement.UP_RIGHT_JUMP);
                movementDirections.add(ChessMove.BoardMovement.LEFT_UP_JUMP);
                movementDirections.add(ChessMove.BoardMovement.RIGHT_UP_JUMP);
                movementDirections.add(ChessMove.BoardMovement.LEFT_DOWN_JUMP);
                movementDirections.add(ChessMove.BoardMovement.DOWN_LEFT_JUMP);
                movementDirections.add(ChessMove.BoardMovement.DOWN_RIGHT_JUMP);
                movementDirections.add(ChessMove.BoardMovement.RIGHT_DOWN_JUMP);
                break;

            case PAWN:
                movementDirections.add(ChessMove.BoardMovement.UP);

                // Although pawns travel forwards, they capture diagonally
                int upper_left_attack_index = currentBitBoardIndex + ChessMove.BoardMovement.UP_LEFT.value();
                int upper_right_attack_index = currentBitBoardIndex + ChessMove.BoardMovement.UP_RIGHT.value();

                ChessPiece upper_left_attack = board.getPiece(upper_left_attack_index);
                ChessPiece upper_right_attack = board.getPiece(upper_right_attack_index);

                if (upper_left_attack != null) {
                    movementDirections.add(ChessMove.BoardMovement.UP_LEFT);
                }

                if (upper_right_attack != null) {
                    movementDirections.add(ChessMove.BoardMovement.UP_RIGHT);
                }

                break;
        }


        for (ChessMove.BoardMovement offset : movementDirections) {
            for (int distance = 0; distance < maxDirectionalMovement; distance++) {
                // First, evaluate the existence of the board position
                int endPositionIndex = currentBitBoardIndex + offset.value();
                if (endPositionIndex < 0 || endPositionIndex >= 64) {
                    continue;
                }

                // Next, evaluate all the pieces moves
                ChessPiece targetCell = board.getPiece(endPositionIndex);
                if (targetCell.getTeamColor() != this.getTeamColor()) {
                    if (this.pieceType == PieceType.PAWN) {
                        // Only evaluate diagonals to 1
                        if (offset == ChessMove.BoardMovement.UP_LEFT) {
                            distance = maxDirectionalMovement;
                        }
                        if (offset == ChessMove.BoardMovement.UP_RIGHT) {
                            distance = maxDirectionalMovement;
                        }
                    }
                } else {
                    pieceMoves.add(new ChessMove(currentPosition, offset));
                }
            }
        }

        return pieceMoves;
    }
}
