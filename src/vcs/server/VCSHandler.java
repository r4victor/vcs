package vcs.server;

import serializer.Compressor;
import server.Handler;
import threaddispatcher.ThreadedTask;
import vcs.protocol.Session;
import vcs.repository.Manager;

import java.io.*;
import java.net.Socket;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class VCSHandler implements Handler {
    private final Manager manager = new Manager();


    @Override
    public ThreadedTask handle(Socket clientSocket) {
        return new ServerSideConnection(clientSocket, new ServerSideSession());
    }

    public class ServerSideConnection extends ThreadedTask {
        private final ServerSideSession session;
        private final Socket socket;

        ServerSideConnection(Socket socket, ServerSideSession session) {
            this.socket = socket;
            this.session = session;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[8192];
            int count;

            try (
                    DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ) {
                while ((count = in.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, count);
                    if (count == buffer.length) {
                        continue;
                    }
                    session.update(byteArrayOutputStream.toByteArray());
                    byte[] response = session.getResponse();
                    out.write(response, 0 , response.length);
                    out.flush();
                    byteArrayOutputStream.reset();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                session.terminate();
            }
        }
    }
}