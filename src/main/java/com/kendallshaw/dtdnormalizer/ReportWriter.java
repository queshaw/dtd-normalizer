package com.kendallshaw.dtdnormalizer;

import java.util.Map;
import java.util.Set;

import org.apache.xml.resolver.CatalogManager;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

import com.kendallshaw.xml.EntityMonitor;
import com.kendallshaw.xml.EntityResolutionMonitor;
import com.kendallshaw.xml.EntityResourceIdentifier;
import com.kendallshaw.xml.TracingCatalog;

public class ReportWriter {

    private static final String SAX_FEATURE =
        org.apache.xerces.impl.Constants.SAX_FEATURE_PREFIX;

    private static final String SAX_PROPERTY =
        org.apache.xerces.impl.Constants.SAX_PROPERTY_PREFIX;

    public static void report(InputSource input, OptionParser opt)
        throws Exception
    {
        CatalogManager cm = new CatalogManager();
        cm.setVerbosity(0);
        cm.setCatalogClassName(TracingCatalog.class.getName());
        IdentifierResolver resolver =
            new IdentifierResolver(opt.getCatalogList(), cm);
        EntityMonitor em = new EntityResolutionMonitor();
        boolean reportEncodings = opt.isReportingEncodings();
        boolean reportEntities = opt.isReportingEntities();
        if (reportEncodings || reportEntities)
            resolver.setEntityMonitor(em);
        resolver.setReportingEncodings(reportEncodings);
        resolver.setReportingEntities(reportEntities);
        DefaultHandler2 dh = new DefaultHandler2();
        XMLReader xr = XMLReaderFactory.createXMLReader();
        xr.setFeature(SAX_FEATURE + "validation", true);
        xr.setFeature(SAX_FEATURE + "namespaces", true);
        xr.setProperty(SAX_PROPERTY + "declaration-handler", dh);
        xr.setContentHandler(dh);
        xr.setEntityResolver(resolver);
        xr.setErrorHandler(dh);
        xr.parse(input);
        boolean hasCatalogs =
            reportCatalogs(opt.isReportingCatalogs(), cm);
        if (opt.isReportingEntities() && opt.isReportingEncodings()) 
            reportBoth(hasCatalogs, opt, em);
        else if (opt.isReportingEntities())
            reportEntities(hasCatalogs, opt, em);
        else if (opt.isReportingEncodings())
            reportEncodings(hasCatalogs, opt, em);
    }

    private static boolean reportCatalogs(boolean include, CatalogManager cm) {
        if (!include)
            return false;
        boolean hasCatalogs = false;
        TracingCatalog catalog = (TracingCatalog) cm.getCatalog();
        Set<String> catalogs = catalog.catalogFiles;
        if (catalogs.size() > 0) {
            hasCatalogs = true;
            System.out.println("Catalogs:\n");
            for (String k : catalogs)
                System.out.println(k);
        }
        return hasCatalogs;
    }

    private static void reportEntities(boolean preceding,
                                       OptionParser opt, EntityMonitor em)
    {
        if (preceding)
            System.out.println("");
        Set<EntityResourceIdentifier> entities = em.entities();
        if (entities.size() > 0)
            System.out.println("Entities:\n");
        for (EntityResourceIdentifier e : entities)
            System.out.printf("%s -> %s\n", publicOrSystemId(e), e.getUri());
    }

    private static void reportEncodings(boolean preceding,
                                        OptionParser opt, EntityMonitor em)
    {
        if (preceding)
            System.out.println("");
        Set<EntityResourceIdentifier> entities = em.entities();
        if (entities.size() > 0)
            System.out.println("Entities:\n");
        for (EntityResourceIdentifier e : entities)
            System.out.printf("%s (%s)\n", e.getUri(), e.getCharset());
    }

    private static void reportBoth(boolean preceding,
                                   OptionParser opt, EntityMonitor em)
    {
        if (preceding)
            System.out.println("");
        Set<EntityResourceIdentifier> entities = em.entities();
        if (entities.size() > 0)
            System.out.println("Entities:\n");
        for (EntityResourceIdentifier e : entities)
            System.out.printf("%s -> %s (%s)\n",
                              publicOrSystemId(e), e.getUri(), e.getCharset());
    }

    public static String publicOrSystemId(EntityResourceIdentifier id) {
        String publicId = id.getPublicId();
        if (publicId == null)
            return id.getSystemId();
        return publicId;
    }
}
