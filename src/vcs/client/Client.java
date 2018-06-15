package vcs.client;

import threaddispatcher.ThreadDispatcher;

public class Client {
    public static void main(String[] args) {
        System.out.println("Welcome to pit!");
        ThreadDispatcher threadDispatcher = new ThreadDispatcher("client");
        threadDispatcher.add(new ClientConnection("localhost", 55555));
    }
}
