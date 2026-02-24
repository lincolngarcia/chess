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

        // Set Global Variables
        Collection<ChessMove> validMoves = new ArrayList<>();
        Collection<ChessMove> pieceMoves = piece.pieceMoves(this.getBoard(), startPosition);

        // Iterate through each piece's moves
        for (ChessMove pieceMove : pieceMoves) {
            ChessGame futureState = new ChessGame(this);
            futureState.setTeamTurn(piece.getTeamColor());
            futureState.updateBoard(pieceMove);

            // Check if the move puts the team in check.
            if (!futureState.isInCheck(futureState.getOffTeamColor())) {
                validMoves.add(pieceMove);
            }
        }

        // Check castling moves
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            for (ChessMove pieceMove : pieceMoves) {
                // Check if the move is a castling move
                int startColumn = pieceMove.getStartPosition().getColumn();
                int endColumn = pieceMove.getEndPosition().getColumn();
                int movementDistance = Math.abs(startColumn - endColumn);
                if (movementDistance == 2) {
                    // Check if the king is in check
                    if (this.isInCheck(piece.getTeamColor())) {
                        validMoves.remove(pieceMove);
                        continue;
                    }

                    // Check the passed square for attacks
                    int passedSquareOffset = pieceMove.getEndPosition().getColumn() == 3 ? -1 : 1;
                    int passedSquareIndex = pieceMove.getStartPosition().getBitboardIndex() + passedSquareOffset;
                    ChessPosition passedSquare = new ChessPosition(passedSquareIndex);
                    if (this.getBoard().canEnemyAttackCell(passedSquare, piece.getTeamColor())) {
                        validMoves.remove(pieceMove);
                        continue;
                    }
                }
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

        // Simulate the move
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            this.getBoard().recordKingPosition(move.getEndPosition(), piece.getTeamColor());
            this.getBoard().setKingMoved(piece.getTeamColor());
        }

        // setHasRookMoved Check
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            // Figure out if the rook is on an applicable starting square
            int startRow = piece.getTeamColor() == TeamColor.WHITE ? 1 : 8;
            int startCol = startPosition.getColumn();

            boolean correctStartRow = startPosition.getRow() == startRow;
            boolean correctStartCol = startCol == 1 || startCol == 8;

            if (correctStartRow && correctStartCol) {
                this.getBoard().setRookMoved(startPosition, piece.getTeamColor());
            }
        }

        // Update the board
        this.getBoard().addPiece(move.getEndPosition(), resultingPiece);
        this.getBoard().addPiece(move.getStartPosition(), null);

        int distance = Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn());

        // Move the rook in the event of castling
        if (resultingPiece.getPieceType() == ChessPiece.PieceType.KING && distance == 2) {
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

        // Remove the pawn in the case of enPassant
        if (this.getBoard().enpassantSquare != null) {
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if (move.getEndPosition().equals(this.getBoard().enpassantSquare)) {
                    // remove the column of the enpassant square at the row of the starting position
                    int enemyPawnRow = startPosition.getRow();
                    int enemyPawnColumn = this.getBoard().enpassantSquare.getColumn();
                    ChessPosition enemyPawnPosition = new ChessPosition(enemyPawnRow, enemyPawnColumn);
                    this.getBoard().addPiece(enemyPawnPosition, null);

                }
            }
        }

        // reset enpassant
        this.getBoard().enpassantSquare = null;

        // conditionally set the enpassantSquare
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int startRow = startPosition.getRow();
            int endRow = move.getEndPosition().getRow();
            int distanceMoved = startRow - endRow;
            if (Math.abs(distanceMoved) == 2) {
                int enPassantOffset = (distanceMoved / 2) * 8;
                this.getBoard().enpassantSquare = new ChessPosition(move.getEndPosition().getBitboardIndex() + enPassantOffset);
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
        return this.getBoard().canEnemyAttackCell(this.getBoard().getKingPosition(teamColor), teamColor);
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
}
