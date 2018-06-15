package vcs.client;

import serializer.Compressor;
import threaddispatcher.ThreadedTask;
import vcs.protocol.Command;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ClientConnection extends ThreadedTask {
    private final String host;
    private final int port;

    public ClientConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        ClientSideSession session = new ClientSideSession();
        byte[] buffer = new byte[8192];
        int count;
        Command c;
        try (
                Socket socket = new Socket(host, port);
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            String fromUser;
            ByteArrayInputStream byteArrayInputStream;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (session.isAlive()) {
                fromUser = scanner.nextLine();
                try {
                    c = CommandFactory.getCommand(fromUser);
                } catch (NoSuchMethodException e) {
                    System.out.println("Unknown command.");
                    continue;
                }
                session.updateOutgoing(c);

                while (true) {
                    byte[] bytes = session.getResponse();
                    if (bytes.length == 0) {
                        break;
                    }

                    byteArrayInputStream = new ByteArrayInputStream(bytes);
                    while ((count = byteArrayInputStream.read(buffer)) > 0) {
                        out.write(buffer, 0, count);
                    }
                    out.flush();
                    byteArrayOutputStream.reset();
                    while ((count = in.read(buffer)) > 0) {
                        byteArrayOutputStream.write(buffer, 0, count);
                        if (in.available() == 0) {
                            break;
                        }
                    }
                    session.update(byteArrayOutputStream.toByteArray());
                }
            }
        } catch (EOFException e) {
            System.out.println("Compression error.");
        } catch (IOException e) {
            System.out.println("Connection error.");
        }
    }
}
