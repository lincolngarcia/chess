package client;

import chess.*;
import chess.converter.ChessFunctions;
import ui.EscapeSequences;
import websocket.ChessGameData;

import java.util.Collection;
import java.util.Objects;
import java.util.Scanner;

public class TUI {
    static String prePrompt = "    > ";

    public static void clear() {
        System.out.print(EscapeSequences.ERASE_SCREEN);
    }

    public static void prompt(String prompt, String[] commands){
        System.out.println(prompt);

        if (commands == null) {
            TUI.error("Invalid help text");
            return;
        }

        String helpText = EscapeSequences.format(
                String.join("\t", commands),
                new String[] {
                        EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY
                });

        System.out.println(helpText);
        System.out.print(prePrompt);
    }

    public static String awaitPrompt(String prompt, String[] commands) {
        TUI.prompt(prompt, commands);

        Scanner scanner = new Scanner(System.in);

        return scanner.nextLine();
    }

    public static void write(String text) {
        System.out.println(text);
    }

    public static void error(String text) {
        System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
        System.out.println(text);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }
    
    public static void printBoard(ChessGameData data, ChessGame.TeamColor perspective, Collection<ChessPosition> highlights) {
        // Use the board from the provided game data
        ChessBoard board = data.game.getBoard();

        String[] columnLabels = {"A", "B", "C", "D", "E", "F", "G", "H"};

        int directionIterator = Objects.equals(perspective, ChessGame.TeamColor.WHITE) ? 1 : -1;
        int headerStartIndex = Objects.equals(perspective, ChessGame.TeamColor.WHITE) ? 0 : 7;

        StringBuilder header = new StringBuilder();
        header.append("    ");
        for (int i = headerStartIndex; i < 8 && i >= 0; i += directionIterator) {
            header.append(columnLabels[i]);
            header.append("  ");
        }
        header.append("  ");
        write(EscapeSequences.format(header.toString(), new String[]{EscapeSequences.SET_BG_COLOR_LIGHT_GREY}));

        StringBuilder boardString = new StringBuilder();
        int startIndex = Objects.equals(perspective, ChessGame.TeamColor.WHITE) ? 0 : 63;
        for (int index = startIndex; index < 64 && index >= 0; index += directionIterator) {
            int row, col;

            if (perspective.equals(ChessGame.TeamColor.WHITE)) {
                row = 7 - index / 8;
                col = index % 8;
            } else {
                row = 7 - (index) / 8;
                col = (index) % 8;
            }

            int startLineColumn = perspective.equals(ChessGame.TeamColor.WHITE) ? 0 : 7;
            if (col == startLineColumn) {
                boardString.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                boardString.append(" ");
                boardString.append(row + 1);
                boardString.append(" ");
                boardString.append(EscapeSequences.RESET_BG_COLOR);
            }

            ChessPosition currentPosition = new ChessPosition(row + 1, col + 1);
            String lightSquare = EscapeSequences.SET_BG_COLOR_WHITE;

            String darkSquare = EscapeSequences.SET_BG_COLOR_DARK_GREY;

            if (highlights != null) {
                if (highlights.contains(currentPosition)) {
                    lightSquare = EscapeSequences.SET_BG_COLOR_YELLOW;
                    darkSquare = EscapeSequences.SET_BG_COLOR_GREEN;
                }
            }

            if ((row + col) % 2 == 0) {
                boardString.append(EscapeSequences.SET_TEXT_COLOR_WHITE);
                boardString.append(darkSquare);
            } else {
                boardString.append(EscapeSequences.SET_TEXT_COLOR_DARK_GREY);
                boardString.append(lightSquare);
            }

            String charCode = "   ";

            ChessPiece targetCell = board.getPiece(new ChessPosition(row + 1, col + 1));
            if (targetCell != null) {
                ChessGame.TeamColor teamColor = targetCell.getTeamColor();
                charCode = switch (targetCell.getPieceType()) {
                    case KING -> ChessFunctions.isWhite(teamColor) ?
                            EscapeSequences.WHITE_KING :
                            EscapeSequences.BLACK_KING;
                    case QUEEN -> ChessFunctions.isWhite(teamColor) ?
                            EscapeSequences.WHITE_QUEEN :
                            EscapeSequences.BLACK_QUEEN;
                    case BISHOP -> ChessFunctions.isWhite(teamColor) ?
                            EscapeSequences.WHITE_BISHOP :
                            EscapeSequences.BLACK_BISHOP;
                    case KNIGHT -> ChessFunctions.isWhite(teamColor) ?
                            EscapeSequences.WHITE_KNIGHT :
                            EscapeSequences.BLACK_KNIGHT;
                    case ROOK -> ChessFunctions.isWhite(teamColor) ?
                            EscapeSequences.WHITE_ROOK :
                            EscapeSequences.BLACK_ROOK;
                    case PAWN -> ChessFunctions.isWhite(teamColor) ?
                            EscapeSequences.WHITE_PAWN :
                            EscapeSequences.BLACK_PAWN;
                };
            }

            boardString.append(charCode);

            int endLineColumn = perspective == ChessGame.TeamColor.WHITE ? 7 : 0;
            if (col == endLineColumn) {
                boardString.append(EscapeSequences.RESET_TEXT_COLOR);
                boardString.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                boardString.append(" ");
                boardString.append(row + 1);
                boardString.append(" ");
                boardString.append(EscapeSequences.RESET_BG_COLOR);
                boardString.append("\n");
            }
        }

        System.out.print(boardString);

        int footerStartIndex = Objects.equals(perspective, ChessGame.TeamColor.WHITE) ? 0 : 7;
        StringBuilder footer = new StringBuilder();
        footer.append("    ");
        for (int i = footerStartIndex; i < 8 && i >= 0; i += directionIterator) {
            footer.append(columnLabels[i]);
            footer.append("  ");
        }
        footer.append("  ");
        write(EscapeSequences.format(footer.toString(), new String[]{EscapeSequences.SET_BG_COLOR_LIGHT_GREY}));
    }
}
