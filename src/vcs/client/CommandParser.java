package vcs.client;

import vcs.protocol.Command;

public interface CommandParser {
    Command parse(String s);
}
