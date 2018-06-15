package vcs.protocol;

import serializer.Serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SerializedPacket implements PacketFormat {
    @Override
    public byte[] pack(Command command) {
        byte[] serializedCommand = Serializer.serialize(command);
        byte[] lengthArray = ByteBuffer.allocate(4).putInt(serializedCommand.length).array();
        byte[] bytes = new byte[serializedCommand.length + lengthArray.length];
        System.arraycopy(lengthArray, 0, bytes, 0, 4);
        System.arraycopy(serializedCommand, 0, bytes, 4, serializedCommand.length);
        return bytes;
    }

    @Override
    public PacketUnpackResult unpack(byte[] bytes) {
        List<Command> commands = new ArrayList<>();
        Command c;
        int used = 0;
        while (used + 4 <= bytes.length) {
            int length = ByteBuffer.wrap(Arrays.copyOfRange(bytes, used, used+4)).getInt();
            used += 4;
            if (used + length > bytes.length) {
                break;
            }
            c = (Command)Serializer.deserialize(Arrays.copyOfRange(bytes, used, used + length));
            used += length;
            commands.add(c);
        }

        return new PacketUnpackResult(used, commands.toArray(new Command[commands.size()]));
    }
}
