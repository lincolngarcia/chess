package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(Board, that.Board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(Board);
    }

    ChessPiece[][] Board;

    public ChessBoard() {
        this.Board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int[] indices = position.toIndexFormat();
        this.Board[indices[0]][indices[1]] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int[] indices = position.toIndexFormat();
        return this.Board[indices[0]][indices[1]];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int rowIndex = 0; rowIndex < 8; rowIndex++) {
            for (int colIndex = 0; colIndex < 8; colIndex++) {
                this.Board[rowIndex][colIndex] = null;
            }
        }
    }

    /**
     * Gets the cell associated with a specific index
     *
     * @return ChessPosition of the new cell
     */
    public ChessPiece getCellByIndex(int index) {
        int row = Math.floorDiv(index, 8);
        int col = index % 8;

        return this.Board[row - 1][col - 1];
    }
}