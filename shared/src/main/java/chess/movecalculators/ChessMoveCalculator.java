package chess.movecalculators;

import chess.*;
import chess.converter.ChessFunctions;

import java.util.*;

/**
 * An abstract class for basing movement
 * calculators based off a location and a
 * board.
 */
public abstract class ChessMoveCalculator {
    // Class Variables
    protected ChessDirection[] movementDirections;
    protected ChessGame.TeamColor teamColor;
    protected int movementDistance;
    protected final ChessBoard board;
    protected final ChessPosition position;
    protected final ChessPiece piece;

    protected static final ChessDirection[] allDirections = new ChessDirection[]{
            ChessDirection.LEFT_UP,
            ChessDirection.UP,
            ChessDirection.RIGHT_UP,
            ChessDirection.LEFT,
            ChessDirection.RIGHT,
            ChessDirection.LEFT_DOWN,
            ChessDirection.DOWN,
            ChessDirection.RIGHT_DOWN
    };

    // Standard Overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessMoveCalculator that)) {
            return false;
        }
        return movementDistance == that.movementDistance &&
                Objects.deepEquals(movementDirections, that.movementDirections) &&
                Objects.equals(board, that.board) &&
                Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(movementDirections), movementDistance, board, position);
    }

    @Override
    public String toString() {
        Collection<ChessMove> moves = this.getPieceMoves();
        Collection<ChessPosition> endPositions = new ArrayList<>();
        for (ChessMove move : moves) {
            endPositions.add(move.getEndPosition());
        }

        StringBuilder board = new StringBuilder();
        board.append("   A B C D E F G H\n");

        for (int index = 0; index < 64; index++) {
            int row = index % 8;
            int col = index / 8;

            if (col == 0) {
                board.append(row + 1);
                board.append(" |");
            }

            String characterCode = ".";
            ChessPosition position = new ChessPosition(row + 1, col + 1);
            ChessPiece targetCell = this.board.getPiece(position);

            // Normal Cells
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
                if (ChessFunctions.isBlack(targetCell.getTeamColor())) {
                    characterCode = characterCode.toLowerCase();
                }
            }

            // Targeted Cells
            if (endPositions.contains(position)) {
                if (targetCell == null) {
                    characterCode = "⊗";
                } else {
                    String[] targetedSymbols;
                    if (ChessFunctions.isWhite(targetCell.getTeamColor())) {
                        targetedSymbols = new String[]{"Ⓚ", "Ⓠ", "Ⓡ", "Ⓑ", "Ⓝ", "Ⓟ"};
                    } else {
                        targetedSymbols = new String[]{"ⓚ", "ⓠ", "ⓡ", "ⓑ", "ⓝ", "ⓟ"};
                    }

                    characterCode = switch (targetCell.getPieceType()) {
                        case KING -> targetedSymbols[0];
                        case QUEEN -> targetedSymbols[1];
                        case ROOK -> targetedSymbols[2];
                        case BISHOP -> targetedSymbols[3];
                        case KNIGHT -> targetedSymbols[4];
                        case PAWN -> targetedSymbols[5];
                    };
                }
            }

            // Piece in question
            if (this.getPosition().equals(position) && targetCell != null) {
                String[] targetedSymbols;
                if (ChessFunctions.isWhite(targetCell.getTeamColor())) {
                    targetedSymbols = new String[]{"𝕂", "ℚ", "ℝ", "𝔹", "ℕ", "ℙ"};

                } else {
                    targetedSymbols = new String[]{"𝕜", "𝕢", "𝕣", "𝕓", "𝕟", "𝕡"};
                }

                characterCode = switch (targetCell.getPieceType()) {
                    case KING -> targetedSymbols[0];
                    case QUEEN -> targetedSymbols[1];
                    case ROOK -> targetedSymbols[2];
                    case BISHOP -> targetedSymbols[3];
                    case KNIGHT -> targetedSymbols[4];
                    case PAWN -> targetedSymbols[5];
                };
            }

            board.append(characterCode);
            board.append("|");

            if (col == 7) {
                board.append(" ");
                board.append(row + 1);
                board.append("\n");
            }
        }

        board.append("   A B C D E F G H\n");
        return board.toString();
    }

    // Constructors
    public ChessMoveCalculator(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;

        this.piece = this.getBoard().getPiece(this.getPosition());
        assert this.piece != null : "Invalid Constructor, teamColor not found";

        this.teamColor = this.piece.getTeamColor();

        this.setMovementDirections();
        this.setMovementDistance();
    }

    public ChessMoveCalculator(ChessBoard board, ChessPosition position, ChessGame.TeamColor teamColor) {
        this.board = board;
        this.position = position;
        this.piece = this.getBoard().getPiece(this.getPosition());
        this.teamColor = teamColor;

        this.setMovementDirections();
        this.setMovementDistance();
    }

// Getters

    /**
     * @return ChessBoard the board associated with the calculator
     */
    protected ChessBoard getBoard() {
        return this.board;
    }

    /**
     * @return ChessPosition the position to calculate from
     */
    protected ChessPosition getPosition() {
        return this.position;
    }

    /**
     * @return the team to perform calculations as
     */
    protected ChessGame.TeamColor getTeamColor() {
        return this.teamColor;
    }

// Setters

    /**
     * Set directions a piece can move in
     */
    abstract void setMovementDirections();

    /**
     * Set maximum movement distance possible
     * for a piece
     */
    abstract void setMovementDistance();

    // Logic Heavy Functions
    public Collection<ChessMove> getPieceMoves() {
        // Variables
        ArrayList<ChessMove> pieceMoves = new ArrayList<>();

        // Loop through all movement directions
        for (ChessDirection direction : this.movementDirections) {
            for (int distance = 1; distance <= this.movementDistance; distance++) {
                int endIndex = this.getPosition().getBitboardIndex() + (distance * direction.value());

                ChessPosition endPosition = new ChessPosition(endIndex);
                ChessMove movement = new ChessMove(this.getPosition(), endPosition);

                // Check if the move exists on the board
                if (isInvalidMove(movement, direction)) {
                    break;
                }

                // Check if the selected cell has a piece
                ChessPiece currentPiece = this.board.getPiece(this.getPosition());
                ChessPiece targetedPiece = this.board.getPiece(endPosition);
                if (targetedPiece != null) {
                    if (targetedPiece.getTeamColor() != currentPiece.getTeamColor()) {
                        pieceMoves.add(movement);
                    }
                    break;
                } else {
                    pieceMoves.add(movement);
                }

            }
        }
        return pieceMoves;
    }

    /**
     * Returns all pieces that can be targeted
     *
     * @return A collection of positions containing targeted pieces
     */
    public Collection<ChessPosition> getTargetedPieces() {
        // Variables
        ArrayList<ChessPosition> targetedPieces = new ArrayList<>();
        Collection<ChessDirection> jumpMoves = new ArrayList<>(List.of(
                ChessDirection.UP_LEFT_JUMP,
                ChessDirection.UP_RIGHT_JUMP,
                ChessDirection.LEFT_UP_JUMP,
                ChessDirection.RIGHT_UP_JUMP,
                ChessDirection.LEFT_DOWN_JUMP,
                ChessDirection.RIGHT_DOWN_JUMP,
                ChessDirection.DOWN_LEFT_JUMP,
                ChessDirection.DOWN_RIGHT_JUMP
        ));

        for (ChessDirection direction : this.movementDirections) {
            for (int distance = 1; distance <= this.movementDistance; distance++) {
                int endIndex = this.getPosition().getBitboardIndex() + (distance * direction.value());

                ChessPosition endPosition = new ChessPosition(endIndex);
                ChessMove movement = new ChessMove(this.getPosition(), endPosition);

                // Check if the move exists on the board
                if (isInvalidMove(movement, direction)) {
                    break;
                }

                // Check if the selected cell has a piece
                ChessPiece targetedPiece = this.board.getPiece(endPosition);
                if (targetedPiece != null) {
                    if (targetedPiece.getTeamColor() != teamColor) {
                        targetedPieces.add(endPosition);
                    }
                    break;
                }
                // If ChessMove is in Knight List, break;
                if (jumpMoves.contains(direction)) {
                    break;
                }
            }
        }
        return targetedPieces;
    }

    protected boolean isInvalidMove(ChessMove move, ChessDirection directionMoved) {
        // Variables
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        int endIndex = endPosition.getBitboardIndex();

        // Move is invalid when index < 0 or greater than 63
        if (endIndex < 0 || endIndex >= 64) {
            return true;
        }

        switch (directionMoved) {

            // Move is invalid when startCol > endCol when moving left
            case ChessDirection.LEFT_UP,
                 ChessDirection.LEFT,
                 ChessDirection.LEFT_DOWN,

                 ChessDirection.LEFT_UP_JUMP,
                 ChessDirection.LEFT_DOWN_JUMP,
                 ChessDirection.UP_LEFT_JUMP,
                 ChessDirection.DOWN_LEFT_JUMP:

                if (startPosition.getColumn() < endPosition.getColumn()) {
                    return true;
                }

                break;

            // Move is invalid when startCol > endCol when moving right
            case ChessDirection.RIGHT_UP,
                 ChessDirection.RIGHT,
                 ChessDirection.RIGHT_DOWN,

                 ChessDirection.RIGHT_UP_JUMP,
                 ChessDirection.RIGHT_DOWN_JUMP,
                 ChessDirection.UP_RIGHT_JUMP,
                 ChessDirection.DOWN_RIGHT_JUMP:

                if (startPosition.getColumn() > endPosition.getColumn()) {
                    return true;
                }
        }

        // Otherwise, return false
        return false;
    }
}