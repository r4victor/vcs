package vcs.server;

import server.Server;

public class VCSServer {
    private final Server server;

    VCSServer(int port) {
        this.server = new Server(port, new VCSHandler());
    }
}
