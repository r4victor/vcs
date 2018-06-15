package vcs.repository;

public class RepositoryFile {
    public final String filePath;
    public final byte[] content;

    private RepositoryFile() {
        this("", null);
    }

    public RepositoryFile(String filePath, byte[] content) {
        this.filePath = filePath;
        this.content = content;
    }
}
