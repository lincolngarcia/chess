package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    int Row;
    int Column;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return Row == that.Row && Column == that.Column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Row, Column);
    }

    public ChessPosition(int row, int col) {
        this.Row = row;
        this.Column = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.Row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return this.Column;
    }

    /**
     * @return the indexes of the location of the cell
     */
    public int[] toIndexFormat() {
        return new int[] { this.Row - 1, this.Column - 1 };
    }

    /**
     * Gets the bitboard position of the Position
     * @return index of the cell on a bitboard
     */
    public int getBitBoardIndex() {
        int[] indices = this.toIndexFormat();
        return indices[0] * 8 + indices[1] - 1;
    }

}
