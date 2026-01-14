package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    ChessPosition startPosition;
    ChessPosition endPosition;
    ChessPiece.PieceType promotionPiece;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(startPosition, chessMove.startPosition) && Objects.equals(endPosition, chessMove.endPosition) && promotionPiece == chessMove.promotionPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }

    @Override
    public String toString() {
        return this.startPosition + " to " + this.endPosition;
    }

    public ChessMove(ChessPosition startPosition, int offset) {
        this.startPosition = startPosition;

        int startIndex = startPosition.getBitBoardIndex();
        int endIndex = startIndex + offset;

        int endRow = Math.floorDiv(endIndex, 8) + 1;
        int endCol = (endIndex % 8) + 1;

        this.endPosition = new ChessPosition(endRow, endCol);
    }

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return this.startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return this.endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return this.promotionPiece;
    }

    /**
     * A board can be represented as:
     * 56 57 58 59 60 61 62 63
     * 48 49 50 51 52 53 54 55
     * 40 41 42 43 44 45 46 47
     * 32 33 34 35 36 37 38 39
     * 24 25 26 27 28 29 30 31
     * 16 17 18 19 20 21 22 23
     *  8  9 10 11 12 13 14 15
     *  0  1  2  3  4  5  6  7
     * Where movement is:
     * +7 +8 +9
     * -1 +0 +1
     * -9 -8 -7
     */
    public enum BoardMovement {
        UP_LEFT(7),
        UP(8),
        UP_RIGHT(9),
        LEFT(-1),
        RIGHT(1),
        DOWN_LEFT(-9),
        DOWN(-8),
        DOWN_RIGHT(-7),

        // TODO: redo these
        UP_LEFT_JUMP(-17),
        UP_RIGHT_JUMP(-15),
        LEFT_UP_JUMP(-10),
        RIGHT_UP_JUMP(-6),
        LEFT_DOWN_JUMP(+6),
        DOWN_LEFT_JUMP(+15),
        DOWN_RIGHT_JUMP(15),
        RIGHT_DOWN_JUMP(17);

        private final int offset;

        BoardMovement(int offset) {
            this.offset = offset;
        }

        public int value() {
            return this.offset;
        }
    }
}
