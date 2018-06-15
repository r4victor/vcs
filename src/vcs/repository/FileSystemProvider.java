package vcs.repository;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileSystemProvider implements DataProvider {
    private final String baseDir;

    FileSystemProvider(String baseDir) {
        this.baseDir = baseDir + '/';
    }

    @Override
    public byte[] read(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(baseDir + filePath));
    }

    @Override
    public void write(RepositoryFile file) throws IOException {
        Path path = Paths.get(baseDir + file.filePath);
        Files.createDirectories(path.getParent());
        Files.write(Paths.get(baseDir + file.filePath), file.content, StandardOpenOption.CREATE);
    }
}
