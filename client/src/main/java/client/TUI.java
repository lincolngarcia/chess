package client;

import chess.ChessBoard;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import ui.EscapeSequences;

import java.io.IOException;

public class TUI {
    static Terminal terminal;
    static String pre_prompt = "    > ";

    static {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clear() {
        terminal.puts(InfoCmp.Capability.clear_screen);
    }

    public static String prompt(String prompt) {
        return prompt(prompt, new String[]{});
    }

    public static String prompt(String prompt, String[] commands) {
        terminal.writer().println(prompt);

        if (commands == null) {
            return "Invalid help text";
        }

        String helpText = EscapeSequences.format(
                String.join("\t", commands),
                new String[] {
                        EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY
                });

        terminal.writer().println(helpText);
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        return reader.readLine(pre_prompt);
    }

    public static void write(String text) {
        terminal.writer().println(text);
    }

    public static void printBoard() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        write(board.toString());
    }
}
