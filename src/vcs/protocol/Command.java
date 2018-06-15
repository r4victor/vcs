package vcs.protocol;

import vcs.repository.RepositoryFile;

import java.util.Arrays;

public class Command {
    public final int type;
    public final int status;
    public final String info;
    public final String[] args;
    public final RepositoryFile[] attachments;

    private Command() {
        this(0, 0, "", null, null);
    }

    public Command(int type, RepositoryFile[] attachments) {
        this(type, 0, "", null, attachments);
    }

    public Command(int type, int status, String info) {
        this(type, status, info, null, null);
    }

    public Command(int type,  String[] args) {
        this(type, 0, "", args, null);
    }

    public Command(int type, int status, String info, String[] args) {
        this(type, status, info, args, null);
    }

    public Command(int type, int status, String info, String[] args, RepositoryFile[] attachments) {
        this.type = type;
        this.status = status;
        this.info = info;
        this.args = args;
        this.attachments = attachments;
    }

    public static RepositoryFile[] concatenateUpdatedDeleted(RepositoryFile[] updated, RepositoryFile[] deleted) {
        RepositoryFile[] attachments = new RepositoryFile[updated.length + deleted.length];
        System.arraycopy(updated, 0, attachments, 0, updated.length);
        System.arraycopy(deleted, 0, attachments, updated.length, deleted.length);
        return attachments;
    }

    public static RepositoryFile[][] splitUpdatedDeleted(RepositoryFile[] attachments) {
        int deletedStartPos = attachments.length;
        for (int i = 0; i < attachments.length; i++) {
            if (attachments[i].content == null) {
                deletedStartPos = i;
                break;
            }
        }
        RepositoryFile[] updated = new RepositoryFile[deletedStartPos];
        RepositoryFile[] deleted = new RepositoryFile[attachments.length - deletedStartPos];
        System.arraycopy(attachments, 0, updated, 0, updated.length);
        System.arraycopy(attachments, deletedStartPos, deleted, 0, deleted.length);
        return new RepositoryFile[][] {updated, deleted};
    }

    @Override
    public String toString() {
        return String.format("%s %s", type, Arrays.toString(args));
    }
}
