package vcs.protocol;


public interface Session {
    void update(byte[] bytes);
    byte[] getResponse();
    boolean isAlive();
}
