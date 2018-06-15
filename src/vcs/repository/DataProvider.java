package vcs.repository;


import java.io.IOException;

public interface DataProvider {
    byte[] read(String filePath) throws IOException;
    void write(RepositoryFile file) throws IOException;
}
