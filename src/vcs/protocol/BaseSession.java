package vcs.protocol;

import vcs.utils.Utils;

import java.util.*;

public abstract class BaseSession implements Session {
    private boolean alive = true;
    private byte[] unused;
    private final PacketFormat packetFormat;
    protected final List<Command> outgoingCommands = new ArrayList<>();

    protected BaseSession() {
        this.packetFormat = new SerializedPacket();
    }

    protected BaseSession(PacketFormat packetFormat) {
        this.packetFormat = packetFormat;
    }

    public void setDead() {
        alive = false;
    }

    private void dispatchCommand(Command c) {
        CommandExecutor ce = getCommandExecutor(c.type);
        if (ce == null) {
            throw new NoSuchElementException();
        }

        ce.execute(c);
    }

    protected abstract CommandExecutor getCommandExecutor(int type);

    @Override
    public void update(byte[] bytes) {
        Command[] incomingCommands = getIncomingCommands(bytes);
        for (Command c: incomingCommands) {
            dispatchCommand(c);
        }

    }

    @Override
    public byte[] getResponse() {
        List<byte[]> packedCommands = new ArrayList<>();
        for (Command c: outgoingCommands) {
            packedCommands.add(packetFormat.pack(c));
        }

        outgoingCommands.clear();
        return Utils.concatenateByteArrays(packedCommands);
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    private Command[] getIncomingCommands(byte[] bytes) {
        byte[] allBytes = getAllBytes(bytes);
        PacketUnpackResult unpackResult = packetFormat.unpack(allBytes);
        updateUnused(allBytes, unpackResult);
        return unpackResult.getCommands();
    }

    private void updateUnused(byte[] allBytes, PacketUnpackResult unpackResult) {
        int unusedLength = allBytes.length - unpackResult.bytesUsed();
        byte[] nextUnused = new byte[unusedLength];
        System.arraycopy(allBytes, unpackResult.bytesUsed(), nextUnused, 0, unusedLength);
        this.unused = nextUnused;
    }

    private byte[] getAllBytes(byte[] bytes) {
        if (unused == null || unused.length == 0) {
            return bytes;
        }
        byte[] allBytes = new byte[unused.length + bytes.length];
        System.arraycopy(unused, 0, allBytes, 0, unused.length);
        System.arraycopy(bytes, 0, allBytes, unused.length, bytes.length);
        return allBytes;
    }
}
