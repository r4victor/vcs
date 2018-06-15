package main;

import serializer.Serializer;
import server.Server;
import threaddispatcher.ThreadDispatcher;
import vcs.client.ClientConnection;
import vcs.protocol.Command;
import vcs.server.VCSHandler;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(55555, new VCSHandler());
        new Thread(server::start).start();
    }
}
