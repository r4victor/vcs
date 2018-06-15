package vcs.repository;

import fileworker.Md5Executor;
import serializer.Serializer;

import java.io.IOException;
import java.util.*;

public class Repository {
    private final String name;
    private int currentVersion = 0;
    private Map<Integer, Map<String, String>> versions = new HashMap<>();
    private Map<Integer, Commit> commits = new HashMap<>();
    private final DataProvider dataProvider;

    Repository(String name, String parentDir) {
        this.name = name;
        this.dataProvider = new FileSystemProvider(parentDir + name);
        versions.put(0, new HashMap<>());
        commits.put(0, new Commit(0, new Date(), new String[] {}, new String[] {}));
    }

    void store() throws IOException {
        storeVersions();
        storeCommits();
    }

    void restore() throws IOException {
        restoreVersions();
        restoreCommits();
    }

    private synchronized void storeVersions() throws IOException {
        Version[] versionsToStore = Version.fromMap(versions);
        byte[] bytes = Serializer.serialize(versionsToStore);
        dataProvider.write(new RepositoryFile(".pit", bytes));
    }

    private synchronized void restoreVersions() throws IOException {
        byte[] bytes = dataProvider.read(".pit");
        Version[] versionsToRestore = (Version[]) Serializer.deserialize(bytes);
        versions = Version.toMap(versionsToRestore);
        currentVersion = versions.size() - 1;
    }

    private synchronized void storeCommits() throws IOException {
        Commit[] commitsArray = commits.values().toArray(new Commit[commits.size()]);
        byte[] bytes = Serializer.serialize(commitsArray);
        dataProvider.write(new RepositoryFile(".history", bytes));
    }

    private synchronized void restoreCommits() throws IOException {
        byte[] bytes = dataProvider.read(".history");
        Commit[] commitsArray = (Commit[])Serializer.deserialize(bytes);
        for (Commit commit: commitsArray) {
            commits.put(commit.getVersion(), commit);
        }
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public synchronized void commit(RepositoryFile ...files) throws IOException {
        Map<String, String> currentVersionIndex = new HashMap<>(versions.get(currentVersion));
        currentVersion += 1;
        versions.put(currentVersion, currentVersionIndex);
        List<String> newFilesPaths = new ArrayList<>();
        for(RepositoryFile f: files) {
            newFilesPaths.add(f.filePath);
            addFile(f);
        }
        commits.put(
                currentVersion, new Commit(currentVersion, new Date(),
                        newFilesPaths.toArray(new String[newFilesPaths.size()]), new String[]{}));

    }

    public synchronized int delete(RepositoryFile ...files) {
        List<String> deleted = new ArrayList<>();
        for (RepositoryFile repositoryFile: files) {
            Map<String, String> versionIndex = versions.get(currentVersion);
            versionIndex.remove(repositoryFile.filePath);
            deleted.add(repositoryFile.filePath);
        }
        commits.get(currentVersion).setDeletedFiles(deleted.toArray(new String[deleted.size()]));
        return deleted.size();
    }

    public RepositoryFile[] getFiles(int previousVersion) {
        return getFiles(previousVersion, currentVersion);
    }

    public RepositoryFile[] getFiles(int from, int to) {
        Map<String, String> previousIndex = versions.get(from);
        Map<String, String> index = versions.get(to);
        List<RepositoryFile> repositoryFiles = new ArrayList<>();
        String previousHash;
        String currentHash;
        for (String filePath: index.keySet()) {
            previousHash = previousIndex.get(filePath);
            currentHash = index.get(filePath);
            if (previousHash == null || !previousHash.equals(currentHash)) {
                try {
                    repositoryFiles.add(new RepositoryFile(filePath, dataProvider.read(index.get(filePath))));
                } catch (IOException e) {
                    return new RepositoryFile[] {};
                }
            }
        }

        return repositoryFiles.toArray(new RepositoryFile[repositoryFiles.size()]);
    }

    public String getLogs() {
        List<String> logList = new ArrayList<>();
        for (Commit c: commits.values()) {
            logList.add(c.toString());
        }

        return String.join("\n\n", logList);
    }

    public RepositoryFile[] getDeleted(int previous) {
        Map<String, String> previousIndex = versions.get(previous);
        Map<String, String> index = versions.get(currentVersion);
        List<RepositoryFile> deletedFiles = new ArrayList<>();
        String currentHash;
        for (String filePath: previousIndex.keySet()) {
            currentHash = index.get(filePath);
            if (currentHash == null) {
                deletedFiles.add(new RepositoryFile(filePath, null));
            }
        }

        return deletedFiles.toArray(new RepositoryFile[deletedFiles.size()]);
    }


    private void addFile(RepositoryFile repositoryFile) throws IOException {
        String hash = Md5Executor.getHash(repositoryFile.content);
        dataProvider.write(new RepositoryFile(hash, repositoryFile.content));
        versions.get(currentVersion).put(repositoryFile.filePath, hash);
    }
}
