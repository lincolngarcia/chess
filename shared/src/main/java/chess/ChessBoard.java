package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    ChessPiece[][] Board;
    ChessPosition whiteKingPOS;
    ChessPosition blackKingPOS;

    private boolean[] kingMoved = new boolean[]{false, false}; // [white, black]
    private boolean[][] rookMoved = new boolean[][]{{false, false}, {false, false}}; //[column][kingIndex]

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

    @Override
    public String toString() {
        StringBuilder board = new StringBuilder();
        board.append("   A B C D E F G H\n");
        for (int row = 7; row >= 0; row--) {
            board.append(row + 1);
            board.append(" |");
            for (int col = 0; col < 8; col++) {
                String characterCode = ".";
                ChessPiece targetCell = this.getPiece(new ChessPosition(row + 1, col + 1));
                if (targetCell != null) {
                    characterCode = switch (targetCell.getPieceType()) {
                        case KING -> "K";
                        case QUEEN -> "Q";
                        case ROOK -> "R";
                        case BISHOP -> "B";
                        case KNIGHT -> "N";
                        case PAWN -> "P";
                    };
                    // White is UpperCase
                    if (targetCell.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        characterCode = characterCode.toLowerCase();
                    }
                }
                board.append(characterCode);
                board.append("|");
            }
            board.append(" ");
            board.append(row + 1);
            board.append("\n");
        }
        board.append("   A B C D E F G H\n");
        return board.toString();
    }

    public ChessBoard() {
        this.Board = new ChessPiece[8][8];
    }

    public ChessBoard(ChessBoard oldBoard) {
        this.Board = new ChessPiece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int index = r * 8 + c;
                ChessPosition position = new ChessPosition(index);
                ChessPiece oldPiece = oldBoard.getPiece(position);
                if (oldPiece == null) {
                    continue;
                }

                ChessPiece piece = new ChessPiece(oldPiece);
                this.addPiece(position, piece);
            }
        }

        ChessPosition whiteKingPOS = oldBoard.getKingPosition(ChessGame.TeamColor.WHITE);
        this.recordKingPosition(whiteKingPOS, ChessGame.TeamColor.WHITE);

        ChessPosition blackKingPOS = oldBoard.getKingPosition(ChessGame.TeamColor.BLACK);
        this.recordKingPosition(blackKingPOS, ChessGame.TeamColor.BLACK);

        this.kingMoved = oldBoard.kingMoved;
        this.rookMoved = oldBoard.rookMoved;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        this.Board[position.getRow() - 1][position.getColumn() - 1] = piece;

        if (piece == null) {
            return;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            this.recordKingPosition(position, piece.getTeamColor());
        }
    }

    /**
     * Returns a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.Board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * records the position of the king for quick retrieval
     * and castling checks
     *
     * @param position the position of the king
     * @param color    the color of the king
     */
    public void recordKingPosition(ChessPosition position, ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            this.whiteKingPOS = position;
            this.kingMoved[0] = true;
        } else {
            this.blackKingPOS = position;
            this.kingMoved[1] = true;
        }

    }

    public void recordRookPosition(ChessPosition position, ChessGame.TeamColor color) {
        int rowIndex = color == ChessGame.TeamColor.WHITE ? 0 : 1;
        int colIndex = position.getColumn() == 1 ? 0 : 1;

        this.rookMoved[rowIndex][colIndex] = true;
    }

    /**
     * returns the position of the king
     *
     * @param color the color of the king
     * @return ChessPosition the position of the king
     */
    public ChessPosition getKingPosition(ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            return this.whiteKingPOS;
        } else {
            return this.blackKingPOS;
        }
    }

    /**
     * returns a collection of piece positions given a team
     *
     * @param color the team color
     * @return Collection of ChessPosition
     */
    public Collection<ChessPosition> getTeamPositions(ChessGame.TeamColor color) {
        Collection<ChessPosition> positions = new ArrayList<>();

        for (int i = 0; i < 64; i++) {
            ChessPosition position = new ChessPosition(i);
            ChessPiece piece = this.getPiece(position);
            if (piece == null) {
                continue;
            }

            if (piece.getTeamColor() == color) {
                positions.add(position);
            }
        }

        return positions;
    }

    public void setKingMoved(ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            this.kingMoved[0] = true;
        }else{
            this.kingMoved[1] = true;
        }
    }

    public boolean hasKingMoved(ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            return this.kingMoved[0];
        }else{
            return this.kingMoved[1];
        }
    }

    public boolean hasRookMoved(ChessGame.TeamColor color, int col) {
        int rowIndex = color == ChessGame.TeamColor.WHITE ? 0 : 1;
        int colIndex = col == 1 ? 0 : 1;

        return this.rookMoved[rowIndex][colIndex];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // Clear the board
        for (int rowIndex = 0; rowIndex < 8; rowIndex++) {
            for (int colIndex = 0; colIndex < 8; colIndex++) {
                this.Board[rowIndex][colIndex] = null;
            }
        }

        this.whiteKingPOS = new ChessPosition(1, 5);
        this.blackKingPOS = new ChessPosition(8, 5);

        // Place black pieces
        this.Board[0][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        this.Board[0][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        this.Board[0][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        this.Board[0][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        this.Board[0][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        this.Board[0][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        this.Board[0][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        this.Board[0][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        for (int col = 0; col < 8; col++) {
            this.Board[1][col] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        }

        // Place white pieces
        this.Board[7][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        this.Board[7][1] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        this.Board[7][2] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        this.Board[7][3] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        this.Board[7][4] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        this.Board[7][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        this.Board[7][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        this.Board[7][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        for (int col = 0; col < 8; col++) {
            this.Board[6][col] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }
    }
}