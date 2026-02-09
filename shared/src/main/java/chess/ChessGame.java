package chess;

import chess.ChessMoveCalculators.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    TeamColor teamToMove = TeamColor.WHITE;
    ChessBoard Board;

    private boolean[] kingMoved = new boolean[]{false, false}; // [white, black]
    private boolean[][] rookMoved = new boolean[][]{{false, false}, {false, false}}; //[column][kingIndex]

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return teamToMove == chessGame.teamToMove && Objects.equals(Board, chessGame.Board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamToMove, Board);
    }

    @Override
    public String toString() {
        return teamToMove + " to move," + "\n" + Board;
    }

    public ChessGame() {
        this.setBoard(new ChessBoard());
        this.getBoard().resetBoard();
    }

    public ChessGame(ChessGame oldGame) {
        this.setBoard(new ChessBoard(oldGame.getBoard()));
        this.setTeamTurn(oldGame.getTeamTurn());
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamToMove;
    }

    /**
     * @return Get the team whose turn it isn't
     */
    public TeamColor getOffTeamColor() {
        if (this.getTeamTurn() == TeamColor.WHITE) {
            return TeamColor.BLACK;
        } else {
            return TeamColor.WHITE;
        }
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamToMove = team;
    }

    /**
     * Alternate the team to move
     */
    public void toggleTeamTurn() {
        if (this.teamToMove == TeamColor.WHITE) {
            this.teamToMove = TeamColor.BLACK;
        } else {
            this.teamToMove = TeamColor.WHITE;
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        // If the piece doesn't exist, no moves can be generated
        ChessPiece piece = this.getBoard().getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        // Set the team to the color of the piece
        this.setTeamTurn(piece.getTeamColor());

        // Set Global Variables
        Collection<ChessMove> validMoves = new ArrayList<>();
        Collection<ChessMove> pieceMoves = piece.pieceMoves(this.getBoard(), startPosition);

        // Iterate through each piece's moves
        for (ChessMove pieceMove : pieceMoves) {
            ChessGame futureState = new ChessGame(this);
            futureState.updateBoard(pieceMove);

            // Check if the move puts the team in check.
            if (!futureState.isInCheck(futureState.getOffTeamColor())) {
                validMoves.add(pieceMove);
            }
        }

        // Add castling moves
        for (int i = 0; i < 2; i++) {
            int column = i == 0 ? 1 : 8;

            if (canTeamCastle(this.getTeamTurn(), column)) {
                ChessPosition kingPOS = this.getBoard().getKingPosition(this.getTeamTurn());
                int kingRow = kingPOS.getRow();

                ChessMove castleMove = new ChessMove(kingPOS, new ChessPosition(kingRow, column == 1 ? 3 : 7));
                validMoves.add(castleMove);
            }
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startingPosition = move.getStartPosition();

        ChessPiece piece = this.getBoard().getPiece(startingPosition);

        if (piece == null) {
            throw new InvalidMoveException();
        }

        if (piece.getTeamColor() != this.getTeamTurn()) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> validMoves = this.validMoves(startingPosition);

        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException();
        } else {
            this.updateBoard(move);
        }

    }

    /**
     * Forces a move (does not check for legality)
     *
     * @param move a move to simulate
     */
    public void updateBoard(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece piece = this.getBoard().getPiece(startPosition);
        if (piece == null) {
            return;
        }

        ChessPiece.PieceType pieceType = move.getPromotionPiece();
        ChessPiece.PieceType resultingPieceType = pieceType == null ? piece.getPieceType() : pieceType;
        ChessPiece resultingPiece = new ChessPiece(piece.getTeamColor(), resultingPieceType);
        int row = this.getTeamTurn() == TeamColor.WHITE ? 1 : 8;
        int rowIndex = row == 1 ? 0 : 1;

        // Simulate the move
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            Board.recordKingPosition(move.getEndPosition(), piece.getTeamColor());
            this.kingMoved[rowIndex] = true;
        }

        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            int columnIndex = Math.floorDiv(startPosition.getColumn(), 8);
            this.rookMoved[rowIndex][columnIndex] = true;
        }


        this.getBoard().addPiece(move.getEndPosition(), resultingPiece);
        this.getBoard().addPiece(move.getStartPosition(), null);

        int distance = Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn());

        if (resultingPiece.getPieceType() == ChessPiece.PieceType.KING && distance == 2) {
            // Move the rook in the event of castling
            int kingEndColumn = move.getEndPosition().getColumn();
            int rookCol = kingEndColumn == 3 ? 1 : 8;
            if (rookCol == 1) {
                ChessPosition newRookPOS = new ChessPosition(row, 4);
                ChessPiece newRook = new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.ROOK);
                this.getBoard().addPiece(newRookPOS, newRook);
                this.getBoard().addPiece(new ChessPosition(row, 1), null);
            } else {
                ChessPosition newRookPOS = new ChessPosition(row, 6);
                ChessPiece newRook = new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.ROOK);
                this.getBoard().addPiece(newRookPOS, newRook);
                this.getBoard().addPiece(new ChessPosition(row, 8), null);
            }
        }

        this.toggleTeamTurn();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return enemyCanAttackCell(this.getBoard().getKingPosition(teamColor));
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        boolean hasEscape = false;
        Collection<ChessPosition> teamPositions = this.getBoard().getTeamPositions(teamColor);

        // Iterate through our pieces to see if any can remove check
        for (ChessPosition teamPiecePosition : teamPositions) {
            if (hasEscape) {
                break;
            }

            Collection<ChessMove> teamPieceMoves = this.validMoves(teamPiecePosition);

            // Iterate through each piece's moves
            for (ChessMove move : teamPieceMoves) {
                ChessGame futureState = new ChessGame(this);
                futureState.updateBoard(move);

                if (!futureState.isInCheck(futureState.getOffTeamColor())) {
                    hasEscape = true;
                }

            }

        }

        return !hasEscape;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (this.isInCheck(teamColor)) {
            return false;
        }

        Collection<ChessPosition> teamPieceLocations = this.getBoard().getTeamPositions(teamColor);
        for (ChessPosition position : teamPieceLocations) {
            Collection<ChessMove> validMoves = this.validMoves(position);
            if (!validMoves.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.Board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.Board;
    }

    /**
     * Check if the required pieces haven't moved
     *
     * @param color the team to check
     * @param col   column the column of the rook
     * @return if the team in question hasn't moved the required pieces
     */
    public boolean castlingPiecesCanCastle(TeamColor color, int col) {
        int kingIndex = color == TeamColor.WHITE ? 0 : 1;
        int columnIndex = Math.floorDiv(col, 8);

        boolean kingHasMoved = kingMoved[kingIndex];
        boolean rookHasMoved = this.rookMoved[kingIndex][columnIndex];

        if (kingHasMoved || rookHasMoved) {
            return false;
        }

        int kingRow = color == TeamColor.WHITE ? 1 : 8;
        int kingCol = 5;

        // Test pieces exist in the right spot
        ChessPosition kingPOS = new ChessPosition(kingRow, kingCol);
        ChessPosition rookPOS = new ChessPosition(kingRow, col);

        ChessPiece king = this.getBoard().getPiece(kingPOS);
        ChessPiece rook = this.getBoard().getPiece(rookPOS);

        if (king == null || rook == null) {
            return false;
        } else if (king.getPieceType() != ChessPiece.PieceType.KING) {
            return false;
        } else if (rook.getPieceType() != ChessPiece.PieceType.ROOK) {
            return false;
        }

        return true;
    }

    /**
     * Check if cell is targeted by an enemy piece
     *
     * @param position the position to check
     * @return if an enemy piece is attacking a cell
     */
    public boolean enemyCanAttackCell(ChessPosition position) {
        Collection<ChessPosition> targetedPiecePositions = new SuperQueenMoveCalculator(this.getBoard(), position).getTargetedPieces();

        // Iterate through all pieces and check their attack paths
        for (ChessPosition targetedPiecePosition : targetedPiecePositions) {
            ChessPiece piece = this.getBoard().getPiece(targetedPiecePosition);
            Collection<ChessMove> enemyMoves = piece.pieceMoves(this.getBoard(), targetedPiecePosition);

            // Iterate through all moves for piece
            for (ChessMove enemyMove : enemyMoves) {
                ChessPosition endPosition = enemyMove.getEndPosition();

                if (endPosition.equals(position)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * determines if a team can castle
     *
     * @param teamColor the color to check
     * @param rookCol   the Column of the rook
     * @return boolean if the team can castle
     */
    public boolean canTeamCastle(TeamColor teamColor, int rookCol) {
        // Cannot castle after moving
        if (!castlingPiecesCanCastle(teamColor, rookCol)) {
            return false;
        }

        // Make sure there's a king in the right spot
        int kingRow = teamColor == TeamColor.WHITE ? 1 : 8;
        int kingCol = 5;
        ChessPosition kingPOS = new ChessPosition(kingRow, kingCol);
        ChessPiece king = this.getBoard().getPiece(kingPOS);
        if (king == null) {
            return false;
        }
        if (king.getPieceType() != ChessPiece.PieceType.KING) {
            return false;
        }

        // Make sure there's a rook in the right spot
        int rookRow = teamColor == TeamColor.WHITE ? 1 : 8;
        ChessPosition rookPOS = new ChessPosition(rookRow, rookCol);
        ChessPiece rook = this.getBoard().getPiece(rookPOS);
        if (rook == null) {
            return false;
        }
        if (rook.getPieceType() != ChessPiece.PieceType.ROOK) {
            return false;
        }

        // Check that castling squares aren't attacked
        // Given 5 -> 8: 5,6,7
        // Given 5 -> 1: 5,4,3,2
        int rookColumn = rookPOS.getColumn();

        int start = 5;
        int end = (rookColumn == 8) ? 8 : 1;
        int step = (rookColumn == 8) ? 1 : -1;

        for (int i = start; i != end; i += step) {
            ChessGame futureState = new ChessGame(this);

            ChessPosition newPosition = new ChessPosition(kingRow, i);

            // Cannot castle through pieces
            if (futureState.getBoard().getPiece(newPosition) != null && i != 5) {
                return false;
            }

            // Put a king in the selected spot for testing attack
            ChessPiece newKing = new ChessPiece(teamColor, ChessPiece.PieceType.KING);
            futureState.getBoard().addPiece(newPosition, newKing);

            // Cannot castle through attacked cells
            if (futureState.enemyCanAttackCell(newPosition)) {
                return false;
            }
        }

        return true;
    }
}
