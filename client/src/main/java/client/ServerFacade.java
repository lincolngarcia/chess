package client;
import server.Server;
import ui.EscapeSequences;

import java.util.Objects;

public class ServerFacade
{
    private Server server;
    public ServerFacade() {
        // Initialize the pre-login
        TUI.clear();
        TUI.write("Welcome to my CS240 Chess Project");

        String[] preLoginOptions = new String[] {
                "help",
                "exit",
                "login",
                "register",
                "pb"
        };

        String command = "";
        while (!Objects.equals(command, "exit")) {
            command = TUI.prompt("please enter a command:", preLoginOptions);

            String formatted;
            switch (command) {
                case "help":
                    formatted = EscapeSequences.format(
                            """
                            register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                            login <USERNAME> <PASSWORD> - to play chess
                            quit - playing chess
                            help - with possible commands
                            """, new String[] {
                            EscapeSequences.SET_TEXT_COLOR_BLUE
                    });
                    TUI.write(formatted);
                    break;
                case "login":
                case "register":
                    formatted = EscapeSequences.format("not more o-auth... ugggghhh", new String[]{
                            EscapeSequences.SET_TEXT_COLOR_BLUE
                    });
                    TUI.write(formatted);
                    break;
                case "pb":
                    formatted = EscapeSequences.format("Aw yeah! that's what I'm talking about :)", new String[]{
                            EscapeSequences.SET_TEXT_COLOR_BLUE
                    });
                    TUI.write(formatted);
                    TUI.printBoard();
                case "exit":
                default:
                    break;
            }

            TUI.write("");
        }

    }
}
