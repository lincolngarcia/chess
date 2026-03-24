package client;

import chess.ChessBoard;
import ui.EscapeSequences;

import java.util.Scanner;

public class TUI {
    static String prePrompt = "    > ";

    public static void clear() {
        System.out.print(EscapeSequences.ERASE_SCREEN);
    }

    public static String prompt(String prompt, String[] commands) {
        System.out.println(prompt);

        if (commands == null) {
            return "Invalid help text";
        }

        String helpText = EscapeSequences.format(
                String.join("\t", commands),
                new String[] {
                        EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY
                });

        System.out.println(helpText);
        System.out.print(prePrompt);
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

    public static void printBoard() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        write(board.toString());
    }
}
