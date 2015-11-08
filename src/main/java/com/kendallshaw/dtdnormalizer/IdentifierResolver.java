// -*- mode: java; coding: utf-8-unix

package com.kendallshaw.dtdnormalizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

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
import com.kendallshaw.net.ResourceUtils;
import com.kendallshaw.xml.EntityMonitor;

public class IdentifierResolver implements XMLEntityResolver, EntityResolver {

    private CatalogManager catalogManager = null;

    private Catalog catalog = null;

    private EntityMonitor entityMonitor;

    private boolean reportingEncodings = false;

    private boolean reportingEntities = false;

    private boolean offline = false;

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
        String ignore = null;
        if (catalogManager == null) {
            ignore =
                System.getProperty("xml.catalog.ignoreMissing", "yes");
            System.setProperty("xml.catalog.ignoreMissing", ignore);
            this.catalogManager = new CatalogManager();
        } else {
            ignore = "yes";
            if (!catalogManager.getIgnoreMissingProperties())
                ignore = "no";
            this.catalogManager = catalogManager;
        }

        if (catalogPath != null)
            this.catalogManager.setCatalogFiles(catalogPath);

        this.catalogManager.setIgnoreMissingProperties("yes".equals(ignore));
        this.catalogManager.setUseStaticCatalog(false);
        this.catalogManager.setRelativeCatalogs(false);
        if (catalogsFound())
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

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    @Override
    public XMLInputSource resolveEntity(XMLResourceIdentifier id)
        throws XNIException, IOException
    {
        final String publicId = id.getPublicId();
        final String systemId = id.getExpandedSystemId();
        String baseId = id.getBaseSystemId();
        XMLInputSource xis = null;
        InputSource is = null;
        try {
            is = resolveEntity(publicId, systemId, baseId);
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
            resolved = resolveSystem(systemId, null);
        else
            resolved = resolvePublic(publicId, systemId, null);
        if (resolved == null)
            resolved = systemId;
        if (isOffline())
            checkOfflineConstraint(resolved);
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

    public InputSource resolveEntity(String publicId,
                                     String systemId, String baseId)
        throws SAXException, IOException
    {
        String resolved = null;
        if (publicId == null)
            resolved = resolveSystem(systemId, baseId);
        else
            resolved = resolvePublic(publicId, systemId, baseId);
        if (resolved == null)
            resolved = systemId;
        if (isOffline())
            checkOfflineConstraint(resolved);
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

    public String resolvePublic(String publicId,
                                String systemId, String baseId)
        throws IOException, MalformedURLException
    {
        if (catalog == null)
            return resolveSystem(systemId, baseId);
        else
            return catalog.resolvePublic(publicId, systemId);
    }

    public String resolveSystem(String systemId, String baseId)
        throws IOException, MalformedURLException
    {
        if (catalog != null)
            return catalog.resolveSystem(systemId);
        else {
            try {
                return ResourceUtils.resolveUri(baseId, systemId);
            } catch (URISyntaxException e) {
                return systemId;
            }
        }
    }

    public String baseId()
        throws IOException
    {
        if (catalog == null)
            return ResourceUtils.userBaseUri();
        else
            return catalog.getCurrentBase();
    }

    private File systemIdFile(String systemId)
        throws SAXException
    {
        if (systemId == null)
            return null;
        try {
            if (systemId.startsWith("file:"))
                return new File(ResourceUtils.fixBrokenWindowsUri(systemId));
            return null;
        } catch (URISyntaxException e) {
            throw new SAXException(e);
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

    private void checkOfflineConstraint(String uri) throws SAXException {
        try {
            boolean plausible = ResourceUtils.plausibleFileResource(uri);
            if (uri != null && !plausible) {
                String msg =
                    uri + " is not suppored in offline mode."; 
                throw new SAXException(msg);
            }
        } catch (URISyntaxException e) {
            throw new SAXException(e);
        }
    }

    private boolean catalogsFound() {
        Vector<?> catalogList = catalogManager.getCatalogFiles();
        // This probably won't happen
        if (catalogList == null)
            return false;
        if (catalogList.size() == 1) {
            String cat = (String) catalogList.get(0);
            return !"./xcatalog".equals(cat);
        }
        return !catalogList.isEmpty();
    }
}

// vim: ff=unix
