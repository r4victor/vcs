package vcs.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Version {
    private final int num;
    private final VersionData[] versionDataArray;

    private Version() {
        this(0, null);
    }

    public Version(int num, VersionData[] versionDataArray) {
        this.num = num;
        this.versionDataArray = versionDataArray;
    }

    public int getNum() {
        return num;
    }

    static Version[] fromMap(Map<Integer, Map<String, String>> map) {
        List<Version> versions = new ArrayList<>();
        for (int verNum: map.keySet()) {
            versions.add(new Version(verNum, VersionData.fromMap(map.get(verNum))));
        }

        return versions.toArray(new Version[versions.size()]);
    }

    static Map<Integer, Map<String, String>> toMap(Version[] versions) {
        Map<Integer, Map<String, String>> map = new HashMap<>();
        for(Version version: versions) {
            map.put(version.num, VersionData.toMap(version.versionDataArray));
        }

        return map;
    }
}
