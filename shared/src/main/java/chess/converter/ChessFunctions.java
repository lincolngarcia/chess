package chess.converter;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChessFunctions {
    // Functions
    static public boolean isWhite(ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.WHITE;
    }

    static public boolean isBlack(ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.BLACK;
    }

    static public int getPawnStartRow(ChessGame.TeamColor color) {
        return isWhite(color) ? 2 : 7;
    }

    static public int getPromotionRow(ChessGame.TeamColor color) {
        return isWhite(color) ? 8 : 1;
    }

    static public int getStartRow(ChessGame.TeamColor color) {
        return isWhite(color) ? 1 : 8;
    }

    // Logic Heavy Functions
    static public List<ChessMove> getAllMoves(ChessGame game, ChessGame.TeamColor color) {
        List<ChessMove> allMoves = new ArrayList<>();
        Collection<ChessPosition> teamPositions = game.getBoard().getTeamPositions(color);
        for (ChessPosition teamPosition : teamPositions) {
            allMoves.addAll(game.validMoves(teamPosition));
        }

        return allMoves;
    }

    static public String exportGameToSAN(ChessGame game) {
        ChessGame temp = new ChessGame();
        StringBuilder san = new StringBuilder();

        // iterate through each move
        int i = 1;
        for (ChessMove move : game.getHistory()) {
            if (++i % 2 == 0) {
                san.append(Math.floorDiv(i, 2));
            }
            san.append(" ");

            // Get the piece that moved
            ChessPiece piece = temp.getBoard().getPiece(move.getStartPosition());

            // make the move
            try {
                temp.makeMove(move);
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }

            // Determine the suffix
            String suffix = "";
            if (temp.isInCheckmate(temp.getTeamTurn())) {
                suffix = "#";
            } else if (temp.isInCheck(temp.getTeamTurn())) {
                suffix = "+";
            }

            san.append(convertMoveToSan(move, piece, suffix));

            if (i % 2 == 1) {
                san.append("\n");
            }


        }

        return san.toString();
    }

    static public String convertMoveToSan(ChessMove move, ChessPiece pieceMoved, String suffix) {
        String sanMove = "";

        String prefix = switch (pieceMoved.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "";
        };

        String startPositionString = move.getStartPosition().toString().toLowerCase();
        String endPositionString = move.getEndPosition().toString().toLowerCase();

        sanMove = prefix + startPositionString + endPositionString + suffix;

        if (move.getPromotionPiece() != null) {
            String promotionPiecePrefix = switch (move.getPromotionPiece()) {
                case KING -> "K";
                case QUEEN -> "Q";
                case BISHOP -> "B";
                case KNIGHT -> "N";
                case ROOK -> "R";
                case PAWN -> "";
            };
            sanMove += "=" + promotionPiecePrefix;
        }

        return sanMove;
    }

    static public void executeRandomMove(ChessGame game, int seed) {
        try {
            List<ChessMove> allMoves = ChessFunctions.getAllMoves(game, game.getTeamTurn());
            int index = Math.abs(allMoves.hashCode() * seed) % allMoves.size();
            game.makeMove(allMoves.get(index));
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
    }
}
