package com.kendallshaw.net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ResourceUtils {

    public static String canonicalFilePath(String path)
        throws IllegalArgumentException, IOException
    {
        if ("-".equals(path)) {
            String msg = "The file path '-' is not supported here.";
            throw new IllegalArgumentException(msg);
        }
        try {
            URI currentDirectory = new File(".").toURI();
            URI uriPath = new URI(path);
            URI uri = currentDirectory.resolve(uriPath);
            if (!"file".equals(uri.getScheme())) {
                String msg = "Invalid file path: " + path;
                throw new IllegalArgumentException(msg);
            }
            return new File(uri).toString();
        } catch (URISyntaxException e) {
            File f = new File(path);
            if (!f.exists()) {
                throw new FileNotFoundException(f + " was not found.");
            } else if (!f.isFile()) {
                throw new IllegalArgumentException(f.toString());
            }
            return f.getCanonicalPath();
        }
    }
}
