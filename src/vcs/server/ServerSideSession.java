package vcs.server;
import vcs.protocol.BaseSession;
import vcs.protocol.Command;
import vcs.protocol.CommandExecutor;
import vcs.repository.Manager;
import vcs.repository.Repository;
import vcs.repository.RepositoryFile;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Map;

public class ServerSideSession extends BaseSession {
    private final Manager manager = new Manager();

    private final Map<Integer, CommandExecutor> commandExecutors = Map.of(
            1, new AddExecutor(),
            2, new CloneExecutor(),
            3, new UpdateExecutor(),
            4, new CommitExecutor(),
            5, new RevertExecutor(),
            6, new LogExecutor(),
            7, new CheckoutExecutor(),
            9, new DeleteExecutor(),
            10, new QuitExecutor()
    );

    ServerSideSession() {
        super();
    }

    void terminate() {
        try {
            manager.storeRepositories();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected CommandExecutor getCommandExecutor(int type) {
        return commandExecutors.get(type);
    }

    class AddExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            String name = c.args[0];
            if (manager.exists(name)) {
                outgoingCommands.add(new Command(1,1,String.format("Repository `%s` already exists.", name)));
                return;
            }
            Repository repository = manager.addRepository(name);
            outgoingCommands.add(new Command(1,0,"Repository has been created successfully.", new String[] {name}));
        }
    }

    class CloneExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            String repoName = c.args[1];
            Command outgoingCommand;
            RepositoryFile[] repositoryFiles;
            if (!manager.exists(repoName)) {
                outgoingCommand = new Command(2, 1, "Repository doesn't exist.");
            } else {
                Repository repository = manager.changeCurrentRepository(repoName);
                repositoryFiles = repository.getFiles(0);
                String[] args = new String[] {String.valueOf(repository.getCurrentVersion())};
                outgoingCommand = new Command(2, 0, "", args, repositoryFiles);
            }
            outgoingCommands.add(outgoingCommand);
        }
    }

    class UpdateExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            Repository repository = manager.getCurrentRepository();
            int userVersion = Integer.parseInt(c.args[0]);
            if (userVersion == repository.getCurrentVersion()) {
                outgoingCommands.add(new Command(3, 1, "Working directory is up-to-date."));
                return;
            }
            RepositoryFile[] updatedFiles = new RepositoryFile[0];
            updatedFiles = repository.getFiles(userVersion);
            RepositoryFile[] deletedFiles = repository.getDeleted(userVersion);
            RepositoryFile[] attachments = Command.concatenateUpdatedDeleted(updatedFiles, deletedFiles);
            outgoingCommands.add(new Command(3,0, "", new String[] {String.valueOf(repository.getCurrentVersion())}, attachments));
        }
    }

    class CommitExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            Repository repo = manager.getCurrentRepository();
            try {
                repo.commit(c.attachments);
            } catch (IOException e) {
                outgoingCommands.add(new Command(4, 2, "Server error. Try to commit later."));
                return;
            }
            outgoingCommands.add(new Command(4, 0, "", new String[] {String.valueOf(repo.getCurrentVersion())}));
        }
    }

    class DeleteExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            Repository repo = manager.getCurrentRepository();
            int deleted = repo.delete(c.attachments);
            String info = String.format("Files deleted: %s", deleted);
            outgoingCommands.add(new Command(9, 0, info));
        }
    }

    class RevertExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {
            RepositoryFile[] repositoryFiles;
            Repository repository = manager.getCurrentRepository();
            int from = Integer.parseInt(c.args[0]);
            int to = Integer.parseInt(c.args[1]);
            if (from == to) {
                outgoingCommands.add(new Command(5, 1, "No changes."));
                return;
            }
            try {
                repositoryFiles = repository.getFiles(from, to);
            } catch (NullPointerException e) {
                outgoingCommands.add(new Command(5, 1, "This version doesn't exist."));
                return;
            }
            outgoingCommands.add(new Command(5,0, "", c.args, repositoryFiles));
        }
    }

    class LogExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            Repository repository = manager.getCurrentRepository();
            outgoingCommands.add(new Command(6, 0, repository.getLogs()));
        }
    }

    class CheckoutExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {
            int userVersion = Integer.parseInt(c.args[0]);
            if (userVersion != manager.getCurrentRepository().getCurrentVersion()) {
                outgoingCommands.add(new Command(7, 1, "Update working directory to be able to commit."));
            } else {
                outgoingCommands.add(new Command(7, 0, ""));
            }
        }
    }

    class QuitExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            try {
                manager.storeRepositories();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outgoingCommands.add(new Command(10, 0, ""));
        }
    }
}
