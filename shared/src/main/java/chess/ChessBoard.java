package chess;

import chess.ChessMoveCalculators.SuperQueenMoveCalculator;

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
    // Class Variables
    private final ChessPiece[][] Board;
    private ChessPosition whiteKingPOS;
    private ChessPosition blackKingPOS;

    private final boolean[] kingMoved = new boolean[]{false, false}; // [white, black]
    private final boolean[][] rookMoved = new boolean[][]{{false, false}, {false, false}}; //[column][kingIndex]

    private ChessPosition enPassantSquare = null;

    // Standard Overrides
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

    // Constructors
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

        this.kingMoved[0] = oldBoard.kingMoved[0];
        this.kingMoved[1] = oldBoard.kingMoved[1];

        // moved rooks
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                ChessGame.TeamColor color = i == 0 ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                int column = j == 0 ? 1 : 8;
                this.rookMoved[i][j] = oldBoard.hasRookMoved(color, column);
            }
        }

        // enPassant square
        if (oldBoard.enPassantSquare != null) {
            this.enPassantSquare = new ChessPosition(oldBoard.enPassantSquare.getBitboardIndex());
        }
    }

    // Getters
    /**
     * Returns the position of the enPassant square
     *
     * @return ChessPosition the position of the square
     */
    public ChessPosition getEnPassantSquare() {
        return this.enPassantSquare;
    }

    /**
     * returns the position of the king of @param color
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
     * Returns a chess piece on the chessboard at @param position
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.Board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * returns a collection of piece positions of @param color
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

    // Setters
    /**
     * Sets the enPassant square
     *
     * @param position the position to store
     */
    public void setEnPassantSquare(ChessPosition position) {
        this.enPassantSquare = position;
    }

    /**
     * Acknowledges king movement for @param color
     * for castling
     *
     * @param color the king's color
     */
    public void setKingMoved(ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            this.kingMoved[0] = true;
        } else {
            this.kingMoved[1] = true;
        }
    }

    /**
     * Acknowledges rook movement for a rook of @param color
     * and @param startPosition
     *
     * @param color       the rook's color
     * @param startColumn the starting column of the rook
     */
    public void setRookMoved(ChessGame.TeamColor color, int startColumn) {
        int startRow = color == ChessGame.TeamColor.WHITE ? 1 : 8;
        assert startColumn == 1 || startColumn == 8 : "Invalid startColumn for Rook";

        int rowIndex = startRow == 1 ? 0 : 1;
        int colIndex = startColumn == 1 ? 0 : 1;

        this.rookMoved[rowIndex][colIndex] = true;
    }

    // Other Functions
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
     * Returns if the king of @param color has moved previously
     *
     * @param color the king's color
     * @return boolean if the king has previously moved
     */
    public boolean hasKingMoved(ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            return this.kingMoved[0];
        } else {
            return this.kingMoved[1];
        }
    }

    /**
     * Returns if the rook of @param color and @param startColumn
     * has moved previously
     *
     * @param color       the king's color
     * @param startColumn the starting column of the rook
     * @return boolean if the king has previously moved
     */
    public boolean hasRookMoved(ChessGame.TeamColor color, int startColumn) {
        int startRow = color == ChessGame.TeamColor.WHITE ? 1 : 8;
        assert startColumn == 1 || startColumn == 8 : "Invalid startColumn for Rook";

        int rowIndex = startRow == 1 ? 0 : 1;
        int colIndex = startColumn == 1 ? 0 : 1;

        return this.rookMoved[rowIndex][colIndex];
    }

    /**
     * Updates the pieces on the board
     *
     * @param move the move to perform
     */
    public void move(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece piece = this.getPiece(startPosition);
        ChessPiece.PieceType promotionPieceType = move.getPromotionPiece();
        ChessPiece.PieceType resultingPieceType = promotionPieceType == null ? piece.getPieceType() : promotionPieceType;
        ChessPiece resultingPiece = new ChessPiece(piece.getTeamColor(), resultingPieceType);

        this.addPiece(move.getEndPosition(), resultingPiece);
        this.addPiece(move.getStartPosition(), null);
    }

    /**
     * records the position of the king for quick retrieval
     *
     * @param position the position of the king
     * @param color    the color of the king
     */
    public void recordKingPosition(ChessPosition position, ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            this.whiteKingPOS = position;
        } else {
            this.blackKingPOS = position;
        }
    }

    // Logic Heavy Functions
    /**
     * Check if cell is targeted by an enemy piece
     *
     * @param position the position to check against
     * @param teamColor the color of the cell at @param position
     * @return if an enemy piece is attacking a cell
     */
    public boolean canTeamAttackCell(ChessPosition position, ChessGame.TeamColor teamColor) {
        // Get the enemy pieces that can potentially target @param position (superQueenCalculator)
        Collection<ChessPosition> targetedPiecePositions = new SuperQueenMoveCalculator(this, position, teamColor).getTargetedPieces(teamColor);

        // Iterate through all pieces and check their attack paths
        for (ChessPosition targetedPiecePosition : targetedPiecePositions) {
            ChessPiece piece = this.getPiece(targetedPiecePosition);
            Collection<ChessMove> enemyMoves = piece.pieceMoves(this, targetedPiecePosition);

            // Iterate through all moves for piece
            for (ChessMove enemyMove : enemyMoves) {
                ChessPosition endPosition = enemyMove.getEndPosition();

                // Return if the team can attack the cell
                if (endPosition.equals(position)) {
                    return true;
                }
            }
        }

        // Otherwise, return false
        return false;
    }

    /**
     * Performs the necessary calculators to move
     * and update the board. Updates rook's have moved
     * if they are captured
     *
     * @param move the move to execute
     */
    public void moveAndCapture(ChessMove move) {
        // Variables
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece capturedPiece = this.getPiece(endPosition);

        // Check if an unmoved rook is being captured
        if (capturedPiece != null) {
            if (capturedPiece.getPieceType() == ChessPiece.PieceType.ROOK) {
                int row = endPosition.getRow();
                int column = endPosition.getColumn();

                boolean cornerRow = row == 1 || row == 8;
                boolean cornerColumn = column == 1 || column == 8;

                if (cornerColumn && cornerRow) {
                    ChessGame.TeamColor color = row == 1 ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                    this.setRookMoved(color, column);
                }
            }
        }

        this.move(move);
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

        // Reset the variables
        this.whiteKingPOS = new ChessPosition(1, 5);
        this.blackKingPOS = new ChessPosition(8, 5);

        this.kingMoved[0] = false;
        this.kingMoved[1] = false;

        this.rookMoved[0][0] = false;
        this.rookMoved[0][1] = false;
        this.rookMoved[1][0] = false;
        this.rookMoved[1][1] = false;

        this.enPassantSquare = null;

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