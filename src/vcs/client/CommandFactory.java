package vcs.client;

import vcs.protocol.Command;

import java.util.Arrays;
import java.util.Map;

public class CommandFactory {
    private final static Map<String, CommandParser> map = Map.of(
            "add", new AddParser(),
            "clone", new CloneParser(),
            "update", new UpdateParser(),
            "commit", new CommitParser(),
            "revert", new RevertParser(),
            "log", new LogParser(),
            "quit", new QuitParser()
    );

    public static Command getCommand(String string) throws NoSuchMethodException {
        String type = string.split("\\s+", 2)[0];
        CommandParser commandParser = map.get(type.toLowerCase());
        if (commandParser == null)
            throw new NoSuchMethodException();
        try {
            return commandParser.parse(string);
        } catch (IllegalArgumentException e) {
            throw new NoSuchMethodException();
        }
    }

    private static String[] getArgValues(String s, int len) {
        String[] tokens = s.split("\\s+", len + 1);
        if (tokens.length != len + 1) {
            throw new IllegalArgumentException();
        }
        return Arrays.copyOfRange(tokens, 1, tokens.length);
    }

    private static String[] getArgValuesWithFlags(String s, int min) {
        String[] tokens = s.split("\\s+");
        if (tokens.length < min + 1) {
            throw new IllegalArgumentException();
        }
        return Arrays.copyOfRange(tokens, 1, tokens.length);
    }

    private static class AddParser implements CommandParser {
        @Override
        public Command parse(String s) {
            return new Command(1, getArgValues(s, 1));
        }
    }

    private static class CloneParser implements CommandParser {
        @Override
        public Command parse(String s) {
            return new Command(2, getArgValuesWithFlags(s, 2));
        }
    }

    private static class UpdateParser implements CommandParser {

        @Override
        public Command parse(String s) {
            return new Command(3, 0, "");
        }
    }

    private static class CommitParser implements CommandParser {

        @Override
        public Command parse(String s) {
            return new Command(4, 0, "");
        }
    }

    private static class RevertParser implements CommandParser {
        @Override
        public Command parse(String s) {
            return new Command(5, getArgValuesWithFlags(s, 1));
        }
    }

    private static class LogParser implements CommandParser {
        @Override
        public Command parse(String s) {
            return new Command(6, 0, "");
        }
    }

    private static class QuitParser implements CommandParser {
        @Override
        public Command parse(String s) {
            return new Command(10, 0, "");
        }
    }

}
