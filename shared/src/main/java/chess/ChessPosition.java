package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final String[] columnLabels = {"A", "B", "C", "D", "E", "F", "G", "H"};
    int row;
    int column;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return this.row == that.row && this.column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return this.columnLabels[this.column - 1] + this.row;
    }

    public ChessPosition(int row, int col) {
        this.row = row;
        this.column = col;
    }

    public ChessPosition(int index) {
        this.row = Math.floorDiv(index, 8) + 1;
        this.column = (index % 8) + 1;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return this.column;
    }

    /**
     * @return the indexes of the location of the cell
     */
    public int[] toIndexFormat() {
        return new int[] { this.row - 1, this.column - 1 };
    }

    /**
     * Gets the bitboard position of the Position
     * @return index of the cell on a bitboard
     */
    public int getBitboardIndex() {
        int[] indices = this.toIndexFormat();
        return indices[0] * 8 + indices[1];
    }

}
