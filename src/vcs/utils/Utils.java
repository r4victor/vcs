package vcs.utils;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Utils {
    public static byte[] concatenateByteArrays(List<byte[]> byteArrays) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (byte[] b: byteArrays) {
            os.write(b, 0, b.length);
        }
        return os.toByteArray();
    }

    public static String versionFromIntToString(int version) {
        String major = String.valueOf(version / 10 + 1);
        String minor = String.valueOf(version % 10);
        return major + "." + minor;
    }

    public static int versionFromStringToInt(String s) {
        int version;
        String[] parts = s.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        return (major - 1) * 10 + minor;
    }
}
