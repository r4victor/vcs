package vcs.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionData {
    private final String filePath;
    private final String hash;

    private VersionData() {
        this(null, null);
    }

    private VersionData(String filePath, String hash) {
        this.filePath = filePath;
        this.hash = hash;
    }

    public static VersionData[] fromMap(Map<String, String> map) {
        List<VersionData> versionDataList = new ArrayList<>();
        for (String filePath: map.keySet()) {
            versionDataList.add(new VersionData(filePath, map.get(filePath)));
        }

        return versionDataList.toArray(new VersionData[versionDataList.size()]);
    }

    public static Map<String, String> toMap(VersionData[] versionDataArray) {
        Map<String, String> map = new HashMap<>();
        for (VersionData versionData: versionDataArray) {
            map.put(versionData.filePath, versionData.hash);
        }
        return map;
    }
}
