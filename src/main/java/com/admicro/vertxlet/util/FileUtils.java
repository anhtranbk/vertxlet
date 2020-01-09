package com.admicro.vertxlet.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileUtils {

    public static String readAll(String path) throws IOException, NullPointerException {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException(path);
        }

        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[1024];
            StringBuilder builder = new StringBuilder();

            int bytesRead;
            while ((bytesRead = reader.read(buffer)) > 0) {
                builder.append(buffer, 0, bytesRead);
            }
            return builder.toString();
        }
    }
}
