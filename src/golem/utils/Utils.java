package golem.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Utils {

    public static String getFile(File file) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int len = 0;

        while ((len = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, len);
        }

        reader.close();
        return sb.toString();
    }
}
