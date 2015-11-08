package com.kendallshaw.dtdnormalizer;

import java.io.IOException;
import java.util.Map;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

import com.kendallshaw.net.ResourceUtils;

public class DocumentHandler extends XniConfigurationSources
                             implements XMLDocumentHandler
{
    private Serialization serializer;

    private XMLDocumentSource documentSource = this;

    private boolean inExternalSubset = false;

    private String baseSystemId = null;

    public DocumentHandler() {
        super();
    }

    public DocumentHandler(final Serialization log, final XniConfiguration cfg)
    {
        super(cfg);
        setConfiguration(cfg);
        setSerializer(log);
    }

    @Override
    public XMLDocumentSource getDocumentSource() {
        return documentSource;
    }

    @Override
    public void setDocumentSource(XMLDocumentSource documentSource) {
        this.documentSource = documentSource;
    }

    public Serialization getSerializer() {
        return serializer;
    }

    public void setSerializer(final Serialization s) {
        serializer = s;
    }

    protected boolean isInExternalSubset() {
        return inExternalSubset;
    }

    protected void setInExternalSubset(final boolean f) {
        inExternalSubset = f;
    }

    public String getBaseSystemId() {
        return baseSystemId;
    }

    public void setBaseSystemId(String baseSystemId) {
        this.baseSystemId = baseSystemId;
    }

    @Override
    public void startDocument(final XMLLocator locator, final String encoding,
                              final NamespaceContext namespaceContext,
                              final Augmentations unused)
        throws XNIException
    {
        String base = locator.getBaseSystemId();
        String expanded = locator.getExpandedSystemId();
        if (expanded != null)
            setBaseSystemId(expanded);
        else if (base != null)
            setBaseSystemId(base);
        final Serialization s = getSerializer();
        s.setLocator(locator);
        s.startDocument("document-type");
    }

    @Override
    public void endDocument(final Augmentations unused) throws XNIException {
        getSerializer().endDocument();
    }

    @Override
    public void xmlDecl(final String version, final String encoding,
                        final String standalone,
                        final Augmentations unused)
        throws XNIException
    {
        getSerializer().xmlDeclaration(version, encoding, standalone);
    }

    @Override
    public void processingInstruction(final String target, final XMLString data,
                                      final Augmentations unused)
        throws XNIException
    {
    }

    @Override
    public void characters(final XMLString text, final Augmentations unused)
        throws XNIException
    {
    }

    @Override
    public void doctypeDecl(final String rootElement, final String publicId,
                            final String systemId, final Augmentations unused)
        throws XNIException
    {
        Map<String, String> inclusionIds = null;
        XMLParserConfiguration cfg = getConfiguration();
        boolean include = false;
        boolean includingAll = false;
        if (XniConfiguration.class.isAssignableFrom(cfg.getClass())) {
            XniConfiguration config = (XniConfiguration) cfg;
            inclusionIds = config.getInclusionIds();
            includingAll = config.isIncludingAll();
            final String mappedSystem =
                systemId == null ? null : inclusionIds.get(systemId);
            final String mappedPublic =
                publicId == null ? null : inclusionIds.get(publicId);
            include = mappedSystem != null || mappedPublic != null;
            config.setIncludingAll(includingAll || include);
            if (include) {
                includingAll = true;
                config.setIncludingAll(true);
            }
            else if (!includingAll) {
                PreParser pp = new PreParser(config);
                try {
                    pp.parse(publicId, systemId, getBaseSystemId());
                } catch (IOException e) {
                    throw new XNIException(e);
                }
            }
        }
        Serialization ser = getSerializer();
        ser.setIncludingAll(include || includingAll);
        XMLEntityResolver er = getConfiguration().getEntityResolver();
        try {
            XMLResourceIdentifier xid =
                ResourceUtils.xniResourceId(publicId, systemId,
                                            getBaseSystemId());
            XMLInputSource xis =
                er.resolveEntity(xid);
            getSerializer().doctypeDeclaration(rootElement, publicId, systemId,
                                               xis.getSystemId());
        } catch (IOException e) {
            throw new XNIException(e);
        }
    }

    @Override
    public void comment(final XMLString text, final Augmentations unused)
        throws XNIException
    {
    }

    @Override
    public void startElement(QName element, XMLAttributes attributes,
                             Augmentations unused)
        throws XNIException
    {
    }

    @Override
    public void endElement(QName element, Augmentations augs)
            throws XNIException {
    }

    @Override
    public void emptyElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {
    }

    @Override
    public void startGeneralEntity(String name,
            XMLResourceIdentifier identifier, String encoding,
            Augmentations augs) throws XNIException {
    }

    @Override
    public void endGeneralEntity(String name, Augmentations augs)
            throws XNIException {
    }

    @Override
    public void textDecl(String version, String encoding, Augmentations augs)
            throws XNIException {
    }

    @Override
    public void ignorableWhitespace(XMLString text, Augmentations augs)
            throws XNIException {
    }

    @Override
    public void startCDATA(Augmentations augs) throws XNIException {
    }

    @Override
    public void endCDATA(Augmentations augs) throws XNIException {
    }
}
