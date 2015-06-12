package com.kendallshaw.dtdnormalizer;

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

public class DocumentHandler implements XMLDocumentHandler {

    private XniConfiguration configuration;

    private Serialization serializer;

    private XMLDocumentSource source = new XMLDocumentSource() {

        @Override
        public void setDocumentHandler(XMLDocumentHandler h) {
            getConfiguration().setDocumentHandler(h);
        }

        @Override
        public XMLDocumentHandler getDocumentHandler() {
            return getConfiguration().getDocumentHandler();
        }
    };

    private boolean inExternalSubset = false;

    public DocumentHandler() {}

    public DocumentHandler(final Serialization log, final XniConfiguration cfg) {
        setConfiguration(cfg);
        setSerializer(log);
    }

    public XniConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final XniConfiguration c) {
        configuration = c;
    }

    @Override
    public void setDocumentSource(final XMLDocumentSource s) {
        source = s;
    }

    public Serialization getSerializer() {
        return serializer;
    }

    public void setSerializer(final Serialization s) {
        serializer = s;
    }

    @Override
    public XMLDocumentSource getDocumentSource() {
        return source;
    }

    protected boolean isInExternalSubset() {
        return inExternalSubset;
    }

    protected void setInExternalSubset(final boolean f) {
        inExternalSubset = f;
    }

    @Override
    public void startDocument(final XMLLocator locator, final String encoding,
                              final NamespaceContext namespaceContext,
                              final Augmentations unused)
        throws XNIException
    {
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
        getSerializer().doctypeDeclaration(rootElement, publicId, systemId);
    }

    @Override
    public void comment(final XMLString text, final Augmentations unused)
        throws XNIException
    {
    }

    @Override
    public void startElement(final QName element, final XMLAttributes attributes,
                             final Augmentations unused) throws XNIException {
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
