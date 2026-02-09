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

        // Set the team to the color of the piece
        this.setTeamTurn(piece.getTeamColor());

        // Set Global Variables
        Collection<ChessMove> validMoves = new ArrayList<>();
        Collection<ChessMove> pieceMoves = piece.pieceMoves(this.getBoard(), startPosition);

        // Iterate through each piece's moves
        for (ChessMove pieceMove : pieceMoves) {
            if (Objects.equals(pieceMove.toString(), "H5 to F7")) {
                System.out.println("heyooo");
            }

            ChessGame futureState = new ChessGame(this);
            futureState.updateBoard(pieceMove);

            // Check if the move puts the team in check.
            if (!futureState.isInCheck(futureState.getOffTeamColor())) {
                validMoves.add(pieceMove);
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
        }else{
            this.updateBoard(move);
        }
    }

    /**
     * Forces a move (does not check for legality)
     * @param move a move to simulate
     */
    public void updateBoard(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece piece = this.getBoard().getPiece(startPosition);
        if (piece == null) {
            return;
        }

        // Simulate the move
        ChessPiece.PieceType pieceType = move.getPromotionPiece();
        ChessPiece.PieceType resultingPieceType = pieceType == null ? piece.getPieceType() : pieceType;
        ChessPiece resultingPiece = new ChessPiece(piece.getTeamColor(), resultingPieceType);

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            Board.recordKingPosition(move.getEndPosition(), piece.getTeamColor());
        }
        this.getBoard().addPiece(move.getEndPosition(), resultingPiece);
        this.getBoard().addPiece(move.getStartPosition(), null);

        this.toggleTeamTurn();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessBoard board = this.getBoard();
        ChessPosition kingPosition = board.getKingPosition(teamColor);

        // Find all the pieces that could have a line of sight to the king
        Collection<ChessPosition> targetedPiecePositions = new SuperQueenMoveCalculator(this.getBoard(), kingPosition).getTargetedPieces();

        // Iterate through all pieces and check their attack paths
        for (ChessPosition targetedPiecePosition : targetedPiecePositions) {
            ChessPiece piece = board.getPiece(targetedPiecePosition);
            Collection<ChessMove> enemyMoves = piece.pieceMoves(board, targetedPiecePosition);

            // Iterate through all moves for piece
            for (ChessMove enemyMove : enemyMoves) {
                ChessPosition endPosition = enemyMove.getEndPosition();

                if (endPosition.equals(kingPosition)) {
                    return true;
                }
            }
        }

        return false;
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
            if (hasEscape) break;

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
