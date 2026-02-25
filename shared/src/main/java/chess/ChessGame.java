package chess;

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
    // Class Variables
    private TeamColor activeTeam = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();
    private final Collection<ChessMove> history = new ArrayList<>();

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    // Standard Overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return activeTeam == chessGame.activeTeam && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeTeam, board);
    }

    @Override
    public String toString() {
        return activeTeam + " to move," + "\n" + board;
    }

    // Constructors
    public ChessGame() {
        this.setBoard(new ChessBoard());
        this.getBoard().resetBoard();
    }

    public ChessGame(ChessGame oldGame) {
        this.setBoard(new ChessBoard(oldGame.getBoard()));
        this.setTeamTurn(oldGame.getTeamTurn());
    }

    // Getters (alphabetical)
    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    /**
     * Returns the history
     *
     * @return Collection<ChessMove> the history of moves
     */
    public Collection<ChessMove> getHistory() {
        return this.history;
    }

    /**
     * Get the team opposite of whose turn it is
     *
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
     * Get the team who can make a move
     *
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.activeTeam;
    }

    // Setters (alphabetical)
    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.activeTeam = team;
    }

    // Other Functions
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return this.getBoard().canTeamAttackCell(this.getBoard().getKingPosition(teamColor), teamColor);
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
     * Prints the game history in an easy-to-read way
     */
    public void printHistory() {
        ChessGame temp = new ChessGame();

        int i = 1;
        for (ChessMove move : this.getHistory()) {
            try {
                temp.makeMove(move);
                System.out.println(Math.floorDiv(++i, 2) + " =====");
                System.out.println(move);
                System.out.print(temp);
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Alternate the team to move
     */
    public void toggleTeamTurn() {
        if (this.activeTeam == TeamColor.WHITE) {
            this.activeTeam = TeamColor.BLACK;
        } else {
            this.activeTeam = TeamColor.WHITE;
        }
    }

    // Logic Heavy Functions
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
    private void updateBoard(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece piece = this.getBoard().getPiece(startPosition);
        if (piece == null) {
            return;
        }

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
                this.getBoard().setRookMoved(piece.getTeamColor(), startCol);
            }
        }

        // Update the board
        this.getBoard().moveAndCapture(move);

        int distance = Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn());
        // Move the rook in the event of castling
        if (piece.getPieceType() == ChessPiece.PieceType.KING && distance == 2) {
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
        if (this.getBoard().getEnPassantSquare() != null) {
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if (move.getEndPosition().equals(this.getBoard().getEnPassantSquare())) {
                    // remove the column of the enPassant square at the row of the starting position
                    int enemyPawnRow = startPosition.getRow();
                    int enemyPawnColumn = this.getBoard().getEnPassantSquare().getColumn();
                    ChessPosition enemyPawnPosition = new ChessPosition(enemyPawnRow, enemyPawnColumn);
                    this.getBoard().addPiece(enemyPawnPosition, null);

                }
            }
        }

        // reset enPassant
        this.getBoard().setEnPassantSquare(null);

        // conditionally set the enPassantSquare
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int startRow = startPosition.getRow();
            int endRow = move.getEndPosition().getRow();
            int distanceMoved = startRow - endRow;
            if (Math.abs(distanceMoved) == 2) {
                int enPassantOffset = (distanceMoved / 2) * 8;
                this.getBoard().setEnPassantSquare(new ChessPosition(move.getEndPosition().getBitboardIndex() + enPassantOffset));
            }
        }

        // Store move in history
        this.history.add(move);

        this.toggleTeamTurn();
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
                    if (this.getBoard().canTeamAttackCell(passedSquare, piece.getTeamColor())) {
                        validMoves.remove(pieceMove);
                    }
                }
            }
        }

        return validMoves;
    }
}
