package vcs.repository;

import vcs.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Commit {
    private int version;
    private String date;
    private String[] deletedFiles;
    private String[] newFiles;

    private Commit() {
        this(0, new Date(), null, null);
    }

    Commit(int version, Date date, String[] newFiles, String[] deletedFiles) {
        SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm:ss dd.MM.yyyy");
        this.version = version;
        this.date = formatDate.format(date);
        this.deletedFiles = deletedFiles;
        this.newFiles = newFiles;
    }

    void setDeletedFiles(String[] deletedFiles) {
        this.deletedFiles = deletedFiles;
    }

    int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        String format = "Version %s – %s.\nChanged files: %s, Deleted files: %s";
        return String.format(format, Utils.versionFromIntToString(version), date, Arrays.toString(newFiles), Arrays.toString(deletedFiles));
    }
}
