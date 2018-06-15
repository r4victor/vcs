package vcs.client;

import vcs.protocol.BaseSession;
import vcs.protocol.Command;
import vcs.protocol.CommandExecutor;
import vcs.repository.RepositoryFile;
import vcs.utils.Utils;

import java.io.IOException;
import java.util.Map;

public class ClientSideSession extends BaseSession {
    private WorkingDirectory workingDirectory;

    private final Map<Integer, CommandExecutor> commandExecutors = Map.of(
            1, new MessageExecutor(),
            2, new CloneResponseExecutor(),
            3, new UpdateResponseExecutor(),
            4, new CommitResponseExecutor(),
            5, new RevertResponseExecutor(),
            6, new MessageExecutor(),
            7, new CommitRequestExecutor(),
            9, new DeleteResponseExecutor(),
            10, new QuitResponseExecutor()
    );


    private final Map<Integer, CommandExecutor> outgoingCommandExecutors = Map.of(
            1, new IdleExecutor(),
            2, new CloneRequestExecutor(),
            3, new UpdateRequestExecutor(),
            4, new CheckoutExecutor(),
            5, new RevertRequestExecutor(),
            6, new LogExecutor(),
            10, new IdleExecutor()
    );

    @Override
    protected CommandExecutor getCommandExecutor(int type) {
        return commandExecutors.get(type);
    }

    void updateOutgoing(Command c) {
        CommandExecutor commandExecutor;
        commandExecutor = outgoingCommandExecutors.get(c.type);

        if (commandExecutor == null) {
            return;
        }

        commandExecutor.execute(c);
    }

    private class IdleExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {
            outgoingCommands.add(c);
        }
    }

    private class MessageExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            System.out.println(c.info);
        }
    }

    private class CloneRequestExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {
            String path = c.args[0];
            if (c.args.length > 2) {
                if (c.args[2].equals(".")) {
                    if (path.endsWith("/")) {
                        path = path + c.args[1];
                    } else {
                        path = path + "/" + c.args[1];
                    }
                }
            }
            workingDirectory = new WorkingDirectory(path);
            workingDirectory.clearDir();
            outgoingCommands.add(c);
        }
    }

    private class CloneResponseExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {
            if (c.status != 0) {
                System.out.println(c.info);
                return;
            }
            workingDirectory.saveFiles(c.attachments);
            workingDirectory.setCurrentVersion(Integer.parseInt(c.args[0]));
            System.out.println("Repository has been cloned successfully.");
        }
    }

    private class UpdateRequestExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            if (workingDirectory == null) {
                System.out.println("You have to clone a repository before update.");
                return;
            }
            outgoingCommands.add(new Command(3, new String[] {String.valueOf(workingDirectory.getCurrentVersion())}));
        }
    }

    private class UpdateResponseExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {
            if (c.status != 0) {
                System.out.println(c.info);
                return;
            }
            RepositoryFile[][] updatedDeleted = Command.splitUpdatedDeleted(c.attachments);
            RepositoryFile[] updated = updatedDeleted[0];
            RepositoryFile[] deleted = updatedDeleted[1];
            workingDirectory.saveFiles(updated);
            workingDirectory.deleteFiles(deleted);
            workingDirectory.setCurrentVersion(Integer.parseInt(c.args[0]));
            System.out.println(String.format("Files updated: %s", updated.length));
            System.out.println(String.format("Files deleted: %s", deleted.length));
        }
    }

    private class CheckoutExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {
            if (workingDirectory == null) {
                System.out.println("You have to clone a repository before commit.");
                return;
            }

            outgoingCommands.add(new Command(7, new String[] {String.valueOf(workingDirectory.getCurrentVersion())}));
        }
    }


    private class CommitRequestExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {
            if (c.status != 0) {
                System.out.println(c.info);
                return;
            }

            if (workingDirectory == null) {
                System.out.println("You have to clone a repository before commit.");
                return;
            }
            RepositoryFile[] newFiles = workingDirectory.getNewFiles();
            RepositoryFile[] deletedFiles = workingDirectory.getDeletedFiles();
            if (newFiles.length == 0 && deletedFiles.length == 0) {
                System.out.println("No changes detected.");
                return;
            }

            outgoingCommands.add(new Command(4, newFiles));

            if (deletedFiles.length != 0) {
                outgoingCommands.add(new Command(9, deletedFiles));
            }
        }
    }

    private class CommitResponseExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            if (c.status != 0) {
                System.out.println(c.info);
                return;
            }
            int versionNum = Integer.parseInt(c.args[0]);
            String version = Utils.versionFromIntToString(versionNum);
            workingDirectory.setCurrentVersion(versionNum);
            System.out.println(String.format("Successful commit. Current version – %s", version));
        }
    }

    private class DeleteResponseExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {

        }
    }


    private class RevertRequestExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {
            if (workingDirectory == null) {
                System.out.println("You have to clone a repository to revert.");
                return;
            }

            String[] args = new String[c.args.length + 1];
            args[0] = String.valueOf(workingDirectory.getCurrentVersion());
            try {
                args[1] = String.valueOf(Utils.versionFromStringToInt(c.args[0]));
            } catch (Exception e) {
                System.out.println("Wrong argument.");
                return;
            }
            System.arraycopy(c.args, 1, args, 2,c.args.length - 1);
            outgoingCommands.add(new Command(5, args));
        }
    }


    private class RevertResponseExecutor implements CommandExecutor {

        @Override
        public void execute(Command c) {
            if (c.status != 0) {
                System.out.println(c.info);
                return;
            }
            int versionNum = Integer.parseInt(c.args[1]);
            String version;
            version = Utils.versionFromIntToString(versionNum);
            workingDirectory.setCurrentVersion(versionNum);
            if (c.args.length > 2 && c.args[2].equals("-hard")) {
                workingDirectory.clearDir();
            }
            workingDirectory.saveFiles(c.attachments);
            System.out.println("Successful revert to version " + version);
        }
    }

    private class LogExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            if (workingDirectory == null) {
                System.out.println("You have to clone a repository to see logs.");
                return;
            }

            outgoingCommands.add(c);
        }
    }

    private class QuitResponseExecutor implements CommandExecutor {
        @Override
        public void execute(Command c) {
            setDead();
            System.out.println("Session is over.");
            try {
                workingDirectory.storeIndex();
                workingDirectory.storeVersion();
            } catch (IOException | NullPointerException ignored) { }
        }
    }

}
