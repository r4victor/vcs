package vcs.protocol;

public interface PacketFormat {
    byte[] pack(Command command);
    PacketUnpackResult unpack(byte[] bytes);
}