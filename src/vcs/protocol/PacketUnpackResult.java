package vcs.protocol;

class PacketUnpackResult {
    private final Command[] commands;
    private final int bytesUsed;

    PacketUnpackResult(int bytesUsed, Command ...commands) {
        this.bytesUsed = bytesUsed;
        this.commands = commands;
    }

    Command[] getCommands() {
        return commands;
    }

    int bytesUsed() {
        return bytesUsed;
    }
}
