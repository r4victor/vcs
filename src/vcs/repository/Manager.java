package vcs.repository;

import serializer.Serializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class Manager {
    private static Map<String, Repository> repositories = null;
    private Repository currentRepository;
    private final String baseDir = "repos/";
    private final String storageFile = ".repos";
    private final static Object lock = new Object();


    public Manager() {

        if (repositories == null) {
            try {
                repositories = new HashMap<>();
                restoreRepositories();
            } catch (IOException ignored) {
            }
        }
    }

    public Repository addRepository(String name) {
        Repository repository = new Repository(name, baseDir);
        synchronized (lock) {
            repositories.put(name, repository);
        }
        return repository;
    }

    public boolean exists(String repositoryName) {
        return repositories.containsKey(repositoryName);
    }

    public Repository changeCurrentRepository(String name) {
        Repository newRepo = repositories.get(name);
        if (newRepo != null) {
            currentRepository = newRepo;
        }

        return newRepo;
    }

    public Repository getCurrentRepository() {
        return currentRepository;
    }

    private void restoreRepositories() throws IOException {
        byte[] bytes = Files.readAllBytes(getStorageFilePath());
        String[] names = (String[]) Serializer.deserialize(bytes);
        for (String name: names) {
            Repository repository = addRepository(name);
            repository.restore();
        }
    }

    public void storeRepositories() throws IOException {
        String[] names = repositories.keySet().toArray(new String[repositories.size()]);
        Files.write(getStorageFilePath(), Serializer.serialize(names));

        for (Repository repository: repositories.values()) {
            repository.store();
        }
    }

    private Path getStorageFilePath() {
        return Paths.get(baseDir + storageFile);
    }
}
