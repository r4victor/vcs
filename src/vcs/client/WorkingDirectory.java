package vcs.client;

import fileworker.FileWorker;
import fileworker.IExecutable;
import fileworker.Md5Executor;
import serializer.Serializer;
import vcs.protocol.Command;
import vcs.repository.RepositoryFile;
import vcs.repository.Version;
import vcs.repository.VersionData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class WorkingDirectory {
   private final FileWorker fileWorker;
   private final Path path;
   private Map<String, String> index = new HashMap<>();
   private int currentVersion = 0;

   WorkingDirectory(String path) {
       this.path = Paths.get(path);
       try {
           Files.createDirectories(this.path);
       } catch (IOException e) {
           e.printStackTrace();
       }
       this.fileWorker = new FileWorker(path, true);
       try {
           loadIndex();
           loadVersion();
       } catch (IOException ignored) {
       }
   }

    int getCurrentVersion() {
        return currentVersion;
    }

    void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }

    void saveFiles(RepositoryFile[] repositoryFiles) {
       for (RepositoryFile repositoryFile: repositoryFiles) {
           try {
               Path p = path.resolve(Paths.get(repositoryFile.filePath));
               Files.createDirectories(p.getParent());
               Files.write(p, repositoryFile.content);
               index.put(repositoryFile.filePath, Md5Executor.getHash(p.toFile()));
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }

   void deleteFiles(RepositoryFile[] repositoryFiles) {
       for (RepositoryFile repositoryFile: repositoryFiles) {
           try {
               Path p = path.resolve(Paths.get(repositoryFile.filePath));
               Files.deleteIfExists(p);
               index.remove(repositoryFile.filePath);
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }

   void storeIndex() throws IOException {
       VersionData[] versionDataArray = VersionData.fromMap(index);
       byte[] bytes = Serializer.serialize(versionDataArray);
       Files.write(path.resolve(".index"), bytes);
   }

   private void loadIndex() throws IOException {
       byte[] bytes = Files.readAllBytes(path.resolve(".index"));
       VersionData[] versionDataArray = (VersionData[]) Serializer.deserialize(bytes);
       index = VersionData.toMap(versionDataArray);
   }

   void storeVersion() throws IOException {
       byte[] bytes = Serializer.serialize(new Version(getCurrentVersion(), null));
       Files.write(path.resolve(".version"), bytes);
   }

    private void loadVersion() throws IOException {
        byte[] bytes = Files.readAllBytes(path.resolve(".version"));
        currentVersion = ((Version)Serializer.deserialize(bytes)).getNum();
    }

   RepositoryFile[] getNewFiles() {
       List<RepositoryFile> newFiles = new ArrayList<>();
       String[] update = updateIndex();
       for (String filePath: update) {
           try {
               newFiles.add(new RepositoryFile(filePath, Files.readAllBytes(path.resolve(Paths.get(filePath)))));
           } catch (IOException ignored) {
           }
       }

       return newFiles.toArray(new RepositoryFile[newFiles.size()]);
   }

   RepositoryFile[] getDeletedFiles() {
       List<RepositoryFile> deletedFiles = new ArrayList<>();
       for (String filePath: index.keySet()) {
           if (!Files.exists(path.resolve(Paths.get(filePath)))) {
               deletedFiles.add(new RepositoryFile(filePath, null));
           }
       }

       for (RepositoryFile repositoryFile: deletedFiles) {
           index.remove(repositoryFile.filePath);
       }

       return deletedFiles.toArray(new RepositoryFile[deletedFiles.size()]);
   }

   private String[] updateIndex() {
       IndexUpdater indexUpdater = new IndexUpdater();
       fileWorker.execute(indexUpdater);
       return indexUpdater.update.toArray(new String[indexUpdater.update.size()]);
   }

   class IndexUpdater implements IExecutable {
       final List<String> update = new ArrayList<>();

       @Override
       public void process(File f) {
           String hash = null;
           try {
               hash = Md5Executor.getHash(f);
           } catch (IOException e) {
               e.printStackTrace();
           }
           String p = path.relativize(f.toPath()).toString();
           String oldHash = index.get(p);
           if (oldHash == null || !oldHash.equals(hash)) {
               index.put(p, hash);
               update.add(p);
           }
       }
   }

   void clearDir() {
       fileWorker.execute(new FileDeleter());
   }

   class FileDeleter implements IExecutable {
       @Override
       public void process(File f) {
           f.delete();
       }
   }
}
