package com.kendallshaw.xml;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogException;

import com.kendallshaw.net.ResourceUtils;

public class TracingCatalog extends Catalog {

    public static Set<String> catalogFiles = new HashSet<String>();

    @Override
    protected synchronized void parseCatalogFile(String fileName)
        throws MalformedURLException, IOException, CatalogException
    {
        super.parseCatalogFile(fileName);
        catalogFiles.add(ResourceUtils.canonicalFilePath(makeAbsolute(fileName)));
    }
}
