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

    @Override
    public String toString() {
        return this.pieceColor.toString() + " " + this.pieceType.toString();
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

        // Loop through each direction until you can't
        for (ChessMove.BoardMovement direction : movementDirections) {
            for (int distance = 0; distance < maxDirectionalMovement; distance++) {
                // Get the new position
                int sign = Integer.signum(direction.value());
                int offset = Math.abs(direction.value()) * (distance + 1) * sign;
                int endPositionIndex = currentPosition.getBitBoardIndex() + offset;
                ChessPosition endPosition = new ChessPosition(endPositionIndex);

                // if it doesn't exist, break
                if (!moveExists(currentPosition, endPosition, direction)) break;

                // if the new square has a piece, add the moves and break;
                ChessPiece targetCell = board.getPiece(endPosition);
                if (targetCell != null) {
                    // If the targeted cell is not your teams color, add the move
                    if (targetCell.getTeamColor() != this.getTeamColor()) {
                        pieceMoves.add(new ChessMove(currentPosition, offset));
                    }
                    break;
                }

                // add the move and move on
                pieceMoves.add(new ChessMove(currentPosition, offset));
            }
        }

        return pieceMoves;
    }

    private boolean moveExists(ChessPosition position,
            ChessPosition endPosition, ChessMove.BoardMovement direction) {

        // See if that index exists on the board
        int endPositionIndex = endPosition.getBitBoardIndex();
        if (endPositionIndex < 0 || endPositionIndex >= 64) return false;

        // See if we jumped from one side of the board to another
        switch (direction) {
            // If moving left, target cell cannot have a higher column
            case UP_LEFT:
            case LEFT:
            case DOWN_LEFT:
            case UP_LEFT_JUMP:
            case LEFT_UP_JUMP:
            case LEFT_DOWN_JUMP:
            case DOWN_LEFT_JUMP:
                if (endPosition.getColumn() >= position.getColumn()) {
                    return false;
                }
                break;

            // If moving right, target cell cannot have a lower column
            case UP_RIGHT:
            case RIGHT:
            case DOWN_RIGHT:
            case UP_RIGHT_JUMP:
            case RIGHT_UP_JUMP:
            case RIGHT_DOWN_JUMP:
            case DOWN_RIGHT_JUMP:
                if (endPosition.getColumn() <= position.getColumn()) {
                    return false;
                }
                break;
        }

        return true;

    }
}
