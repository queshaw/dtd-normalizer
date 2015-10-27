package com.kendallshaw.dtdnormalizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.kendallshaw.xml.EntityMonitor;

public class IdentifierResolver implements XMLEntityResolver, EntityResolver {

    private CatalogManager catalogManager = null;

    private Catalog catalog = null;

    private EntityMonitor entityMonitor;

    private boolean reportingCatalogs = false;

    private boolean reportingEncodings = false;

    private boolean reportingEntities = false;

    public IdentifierResolver() throws Exception {
        this(null);
    }

    public IdentifierResolver(final String catalogPath)
        throws Exception
    {
        this(catalogPath, null);
    }

    public IdentifierResolver(final String catalogPath,
                              final CatalogManager catalogManager)
        throws Exception
    {
        if (catalogManager == null)
            this.catalogManager = new CatalogManager();
        else
            this.catalogManager = catalogManager;

        if (catalogPath != null)
            this.catalogManager.setCatalogFiles(catalogPath);

        String ignore = System.getProperty("xml.catalog.ignoreMissing", "yes");
        this.catalogManager.setIgnoreMissingProperties("yes".equals(ignore));
        this.catalogManager.setUseStaticCatalog(false);
        this.catalogManager.setRelativeCatalogs(false);
        catalog = this.catalogManager.getCatalog();
    }

    public boolean isReportingEncodings() {
        return reportingEncodings;
    }

    public void setReportingEncodings(boolean reportingEncodings) {
        this.reportingEncodings = reportingEncodings;
    }

    public boolean isReportingEntities() {
        return reportingEntities;
    }

    public void setReportingEntities(boolean reportingEntities) {
        this.reportingEntities = reportingEntities;
    }

    public EntityMonitor getEntityMonitor() {
        return entityMonitor;
    }
    public void setEntityMonitor(EntityMonitor entityMonitor) {
        this.entityMonitor = entityMonitor;
    }

    @Override
    public XMLInputSource resolveEntity(XMLResourceIdentifier id)
        throws XNIException, IOException
    {
        final String publicId = id.getPublicId();
        final String systemId = id.getExpandedSystemId();
        String baseId = catalog.getCurrentBase();
        XMLInputSource xis = null;
        InputSource is = null;
        try {
            is = resolveEntity(publicId, systemId);
        } catch (SAXException e) {
            throw new XNIException(e);
        }
        if (is.getCharacterStream() == null) {
            xis = new XMLInputSource(id);
        } else {
            xis = new XMLInputSource(publicId, is.getSystemId(), baseId);
            xis.setCharacterStream(is.getCharacterStream());
            xis.setEncoding(is.getEncoding());
        }
        return xis;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException, IOException
    {
        String resolved = null;
        if (publicId == null)
            resolved = catalog.resolveSystem(systemId);
        else
            resolved = catalog.resolvePublic(publicId, systemId);
        if (resolved == null)
            resolved = systemId;

        File f = systemIdFile(resolved);
        InputSource is = new InputSource();
        is.setSystemId(resolved);
        if (f != null) {
            byte[] bytes = fileBytes(f);
            if (bytes != null) {
                CharsetDetector detector = new CharsetDetector();
                detector.setText(bytes);
                CharsetMatch cm = detector.detect();
                is.setCharacterStream(cm.getReader());
                is.setSystemId(f.toURI().toASCIIString());
                is.setEncoding(cm.getName());
                EntityMonitor em = getEntityMonitor();
                if (em != null) {
                    if (publicId == null)
                        em.resolveEntity(resolved, cm.getName());
                    else
                        em.resolveEntity(publicId, resolved, cm.getName());
                }
            }
        }
        return is;
    }

    private File systemIdFile(String systemId) {
        URI uri = null;
        try {
            uri = new URI(systemId);
            if ("file".equals(uri.getScheme()))
                return new File(uri);
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private byte[] fileBytes(File f) throws IOException {
        FileInputStream fis = null;
        FileChannel fch = null;
        ByteBuffer bb = null;
        byte[] bytes = null;
            try {
                fis = new FileInputStream(f);
                fch = fis.getChannel();
                Long size = Long.valueOf(fch.size());
                bb = ByteBuffer.allocate(size.intValue());
                fch.read(bb);
                bb.rewind();
                bytes = bb.array();
            } finally {
                if (fch != null)
                    fch.close();
                if (fis != null)
                    fis.close();
            }
        return bytes;
    }
}
