package com.kendallshaw.net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.XMLResourceIdentifier;

public class ResourceUtils {

    private static final Pattern FILE_URI_REGEX =
        Pattern.compile("^file:(?:/{0,4})?([a-zA-Z])[|:]/(.*)$");

    private static String userBaseUri;

    private static URI currentDirectory;

    public static URI currentDirectoryUri() {
        try {
            if (currentDirectory == null)
                currentDirectory = new File(".").getCanonicalFile().toURI();
            return currentDirectory;
        } catch (IOException e) {
            // Should not happen
            throw new RuntimeException(e);
        }
    }

    public static String userBaseUri()
        throws IllegalArgumentException, IOException
    {
        if (userBaseUri == null) {
            URI path = new File("resource").toURI();
            userBaseUri = path.toString();
        }
        return userBaseUri;
    }

    public static String canonicalFilePath(String path)
        throws IllegalArgumentException, IOException
    {
        URI uri = fileUri(path);
        return new File(uri).toString();
//        if ("-".equals(path)) {
//            String msg = "The file path '-' is not supported here.";
//            throw new IllegalArgumentException(msg);
//        }
//        try {
//            if (currentDirectory == null)
//                currentDirectory = new File(".").toURI();
//            URI uriPath = new URI(path);
//            URI uri = currentDirectory.resolve(uriPath);
//            if (!"file".equals(uri.getScheme())) {
//                String msg = "Invalid file path: " + path;
//                throw new IllegalArgumentException(msg);
//            }
//            return new File(uri).toString();
//        } catch (URISyntaxException e) {
//            File f = new File(path);
//            if (!f.exists()) {
//                throw new FileNotFoundException(f + " was not found.");
//            } else if (!f.isFile()) {
//                throw new IllegalArgumentException(f.toString());
//            }
//            return f.getCanonicalPath();
//        }
    }

    public static String resolveUri(String base, String path)
        throws URISyntaxException
    {
        URI uri = base == null ? currentDirectoryUri() : new URI(base);
        return uri.resolve(path).toString();
    }

    public static URI fileUri(String path)
        throws IllegalArgumentException, IOException
    {
        if ("-".equals(path)) {
            String msg = "The file path '-' is not supported here.";
            throw new IllegalArgumentException(msg);
        }
        try {
            if (currentDirectory == null)
                currentDirectory = new File(".").toURI();
            URI uriPath = new URI(path);
            URI uri = currentDirectory.resolve(uriPath);
            if (!"file".equals(uri.getScheme())) {
                String msg = "Invalid file path: " + path;
                throw new IllegalArgumentException(msg);
            }
            return uri;
        } catch (URISyntaxException e) {
            File f = new File(path);
            if (!f.exists()) {
                throw new FileNotFoundException(f + " was not found.");
            } else if (!f.isFile()) {
                throw new IllegalArgumentException(f.toString());
            }
            return f.toURI();
        }
    }

    public static XMLResourceIdentifier xniResourceId(String publicId,
                                                      String systemId)
        throws IOException
    {
        return xniResourceId(publicId, systemId, userBaseUri);
    }

    public static XMLResourceIdentifier xniResourceId(String publicId,
                                                      String systemId,
                                                      String baseId)
        throws IOException
    {
        XMLResourceIdentifier xid = new XMLResourceIdentifierImpl();
        xid.setPublicId(publicId);
        xid.setLiteralSystemId(systemId);
        xid.setBaseSystemId(baseId);
        try {
            xid.setExpandedSystemId(resolveUri(baseId, systemId).toString());
        } catch (URISyntaxException e) {
            xid.setExpandedSystemId(systemId);
        }
        return xid;
    }

    public static URI fixBrokenWindowsUri(String path)
        throws URISyntaxException
    {
        if (path == null || !path.startsWith("file:"))
            return null;
        Matcher m = FILE_URI_REGEX.matcher(path);
        if (m.find()) {
            try {
                return new URI(String.format("file:///%s:/%s",
                                             m.group(1), m.group(2)));
            } catch (URISyntaxException e) {
            }
        }
        return new URI(path);
    }

    public static boolean plausibleFileResource(String path)
        throws URISyntaxException
    {
        if (path == null)
            throw new NullPointerException("A null URI was encountered.");
        URI uri = null;
        if (!path.startsWith("file:"))
            uri = new URI(path);
        else
            uri = fixBrokenWindowsUri(path);
        if (uri.isOpaque())
            return false;
        if (!uri.isAbsolute())
            return true;
        if ("file".equals(uri.getScheme()))
            return true;
        return false;
    }
}
