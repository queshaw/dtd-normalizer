package com.kendallshaw.dtdnormalizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

public class IdentifierResolver implements XMLEntityResolver, EntityResolver2 {

    private final static CatalogManager CM = new CatalogManager();
    private Catalog catalog = null;

    protected IdentifierResolver() { }

    public IdentifierResolver(final String catalogPath) throws Exception {
        CM.setIgnoreMissingProperties(true);
        if (catalogPath != null) {
            CM.setCatalogFiles(catalogPath);
        }
        CM.setUseStaticCatalog(false);
        catalog = CM.getCatalog();
    }

    @Override
    public XMLInputSource resolveEntity(XMLResourceIdentifier id)
            throws XNIException, IOException {
        final String publicId = id.getPublicId();
        final String systemId = id.getExpandedSystemId();
        final String base = catalog.getCurrentBase();
        String resolved = null;
        if (publicId == null)
            resolved = catalog.resolveSystem(systemId);
        else
            resolved = catalog.resolvePublic(publicId, systemId);
        if (resolved == null)
            return new XMLInputSource(id);
        else
            return new XMLInputSource(publicId, resolved, base);
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String resolved = null;
        if (publicId == null)
            resolved = catalog.resolveSystem(systemId);
        else
            resolved = catalog.resolvePublic(publicId, systemId);
        if (resolved == null)
            resolved = systemId;
        InputSource is = new InputSource();
        is.setSystemId(resolved);
        is.setByteStream(new FileInputStream(new File(resolved)));
        return is;
    }

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        StringReader sr = new StringReader("");
        InputSource is = new InputSource();
        is.setCharacterStream(sr);
        is.setSystemId("urn:dtd:internal");
        return is;
    }

    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
            throws SAXException, IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
