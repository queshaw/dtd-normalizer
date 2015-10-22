package com.kendallshaw.dtdnormalizer;

import java.io.Writer;
import java.net.URI;

import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;

public interface Serialization {

    XMLLocator getLocator();

    void setLocator(XMLLocator loc);

    void resetTargetResource(URI uri) throws Exception;

    Writer getSerializationWriter();

    void setSerializationWriter(Writer w);

    void startDocument(String root) throws XNIException;

    void endDocument() throws XNIException;

    void xmlDeclaration(String version, String encoding, String standalone)
        throws XNIException;

    void doctypeDeclaration(String root, String publicId, String systemId)
        throws XNIException;

    void textDeclaration(String version, String encoding)
        throws XNIException;

    void comment(String fmt, Object... args)
        throws XNIException;

    void processingIntruction(String target, XMLString data)
        throws XNIException;

    void startConditionalSection(String condition)
        throws XNIException;

    void endConditionalSection();

    void startExternalSubset()
        throws XNIException;

    void endExternalSubset()
        throws XNIException;

    void internalEntityDeclaration(String name,
                                   XMLString text, XMLString rawText,
                                   boolean includeOverride)
        throws XNIException;

    void externalEntityDeclaration(String name,
                                   String publicId, String systemId)
        throws XNIException;


    void unparsedEntityDeclaration(String name,
                                   String publicId, String systemId,
                                   String notation)
        throws XNIException;

    void startEntity(String name)
        throws XNIException;

    void endEntity()
        throws XNIException;

    void elementDeclaration(String name, String contentModel)
        throws XNIException;

    void startAttributeListDeclaration(String name)
        throws XNIException;

    void endAttributeListDeclaration()
        throws XNIException;

    void attributeDeclaration(String name, String type,
                              String[] enumeration, String defaultType,
                              XMLString defaultValue,
                              XMLString rawDefaultValue)
        throws XNIException;

    void notationDeclaration(String name, String publicId, String systemId)
        throws XNIException;

    void redefinition(String entityName) throws XNIException;

    void startElement(String name) throws XNIException;

    void endElement() throws XNIException;

    void emptyElement(String name) throws XNIException;

    void element(String name, String text) throws XNIException;

    void attribute(String name, String text) throws XNIException;

    void text(String text) throws XNIException;

    void stackTrace(Exception e) throws XNIException;

    void flush() throws XNIException;
}
