package com.kendallshaw.dtdnormalizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDTDContentModelSource;
import org.apache.xerces.xni.parser.XMLDTDSource;

public class DtdHandler implements XMLDTDHandler, XMLDTDContentModelHandler {

    private XniConfiguration configuration = new XniConfiguration();

    private Serialization serializer;

    private static enum EntityType {INTERNAL, EXTERNAL};

    private Map<String, EntityType> entityNames =
            new Hashtable<String, EntityType>();

    private Set<String> inclusionPublicIds =
        new HashSet<String>();

    private int inclusionDepth = 0;

    private Set<String> inclusionEntityNames =
            new HashSet<String>();

    private Stack<String> entityStack = new Stack<String>();

    private XMLLocator locator;

    private String currentEntity = null;

    private XMLDTDSource dtdSource = new XMLDTDSource() {

        @Override
        public XMLDTDHandler getDTDHandler() {
            return configuration.getDTDHandler();
        }

        @Override
        public void setDTDHandler(XMLDTDHandler h) {
            configuration.setDTDHandler(h);
        }
    };

    private XMLDTDContentModelSource cmSource = new XMLDTDContentModelSource() {

        @Override
        public XMLDTDContentModelHandler getDTDContentModelHandler() {
            return configuration.getDTDContentModelHandler();
        }

        @Override
        public void setDTDContentModelHandler(XMLDTDContentModelHandler h) {
            configuration.setDTDContentModelHandler(h);
        }
    };

    public DtdHandler() throws Exception {
    }

    public DtdHandler(final Serialization log, final XniConfiguration cfg)
        throws Exception
    {
        setSerializer(log);
        setConfiguration(cfg);
    }

    protected XMLLocator getLocator() {
        return locator;
    }

    protected void setLocator(XMLLocator l) {
        locator = l;
    }

    public XniConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(XniConfiguration c) {
        configuration = c;
    }

    public Serialization getSerializer() {
        return serializer;
    }

    public void setSerializer(Serialization logger) {
        this.serializer = logger;
    }

    public Set<String> inclusionPublicIds() {
        return inclusionPublicIds;
    }

    // XMLDTDHandler protocol

    @Override
    public XMLDTDSource getDTDSource() { return dtdSource; }

    @Override
    public void setDTDSource(XMLDTDSource source) { dtdSource = source; }

    @Override
    public void startDTD(XMLLocator locator, Augmentations augmentations)
            throws XNIException {
        setLocator(locator);
        getSerializer().setLocator(locator);
        entityStack.push("DTD");
    }

    @Override
    public void endDTD(Augmentations augmentations) throws XNIException {
        getSerializer().flush();
        entityStack.pop();
    }

    @Override
    public void textDecl(final String version, final String encoding,
                         final Augmentations augmentations)
        throws XNIException
    {
        getSerializer().textDeclaration(version, encoding);
    }

    @Override
    public void startConditional(final short type, final Augmentations unused)
        throws XNIException
    {
        final String cond = type == CONDITIONAL_IGNORE ? "ignore" : "include";
        getSerializer().startConditionalSection(cond);
    }

    @Override
    public void endConditional(final Augmentations unused)
        throws XNIException
    {
        getSerializer().endConditionalSection();
    }

    @Override
    public void processingInstruction(final String target, final XMLString data,
                                      final Augmentations unused)
        throws XNIException
    {
        getSerializer().processingIntruction(target, data);
    }

    @Override
    public void internalEntityDecl(final String name, final XMLString text,
                                   final XMLString rawText,
                                   final Augmentations unused)
        throws XNIException
    {
        final Serialization s = getSerializer();
        final String suffix = name.replaceFirst("% *", "");
        if (entityNames.containsKey(name)) {
            s.redefinition(name);
        } else {
            entityNames.put(name, EntityType.INTERNAL);
            s.internalEntityDeclaration(suffix, text, rawText);
        }
    }

    @Override
    public void externalEntityDecl(String name,
                                   XMLResourceIdentifier id,
                                   Augmentations augmentations)
        throws XNIException
    {
        final Serialization s = getSerializer();
        final String suffix = name.replaceFirst("% *", "");
        if (entityNames.containsKey(name)) {
            s.redefinition(name);
        } else {
            entityNames.put(name, EntityType.EXTERNAL);
            final String systemId = id.getLiteralSystemId();
            final String publicId = id.getPublicId();
            if (inclusionPublicIds().contains(publicId))
                inclusionEntityNames.add(name);
            getSerializer().externalEntityDeclaration(suffix, publicId, systemId);
        }
    }

    @Override
    public void startExternalSubset(XMLResourceIdentifier id,
            Augmentations augmentations) throws XNIException {
        getSerializer().startExternalSubset();
    }

    @Override
    public void endExternalSubset(Augmentations augmentations)
            throws XNIException {
        getSerializer().endExternalSubset();
    }

    @Override
    public void startParameterEntity(String name,
                                     XMLResourceIdentifier id,
                                     String encoding,
                                     Augmentations augmentations)
        throws XNIException
    {
        if (EntityType.EXTERNAL == entityNames.get(name)) {
            if (inclusionEntityNames.contains(name))
                ++inclusionDepth;
            getSerializer().startEntity(name, inclusionDepth > 0);
            entityStack.push(name);
        }
    }

    @Override
    public void endParameterEntity(String name, Augmentations augmentations)
            throws XNIException {
        if (EntityType.EXTERNAL == entityNames.get(name)) {
            if (inclusionEntityNames.contains(name))
                --inclusionDepth;
            getSerializer().endEntity(inclusionDepth > 0);
            entityStack.pop();
        }
    }

    @Override
    public void elementDecl(String name, String contentModel,
            Augmentations augmentations) throws XNIException {
        getSerializer().elementDeclaration(name, contentModel);
    }

    @Override
    public void startAttlist(String elementName,
                             Augmentations augmentations)
        throws XNIException
    {
        getSerializer().startAttributeListDeclaration(elementName);
    }

    @Override
    public void endAttlist(Augmentations augmentations) throws XNIException {
        getSerializer().endAttributeListDeclaration();
    }

    @Override
    public void attributeDecl(String elementName, String attributeName,
                              String type, String[] enumeration,
                              String defaultType,
                              XMLString defaultValue,
                              XMLString rawDefaultValue,
                              Augmentations augmentations)
        throws XNIException
    {
        getSerializer().attributeDeclaration(attributeName, type,
                                         enumeration, defaultType,
                                         defaultValue);
    }

    @Override
    public void comment(XMLString text, Augmentations augmentations)
            throws XNIException
    {
    }

    @Override
    public void unparsedEntityDecl(String name,
            XMLResourceIdentifier identifier, String notation,
            Augmentations augmentations) throws XNIException {
    }

    @Override
    public void notationDecl(String name, XMLResourceIdentifier identifier,
            Augmentations augmentations) throws XNIException {
    }

    @Override
    public void ignoredCharacters(XMLString text, Augmentations augmentations)
            throws XNIException {
    }

    // XMLDTDContentModelHandler protocol

    @Override
    public XMLDTDContentModelSource getDTDContentModelSource() {
        return cmSource;
    }

    @Override
    public void setDTDContentModelSource(XMLDTDContentModelSource source) {
        cmSource = source;
    }

    @Override
    public void startContentModel(String elementName,
                                  Augmentations augmentations)
        throws XNIException
    {
        final Serialization s = getSerializer();
        s.startElement("content-model");
        s.attribute("element", elementName);
    }

    @Override
    public void endContentModel(Augmentations augmentations)
            throws XNIException {
        getSerializer().endElement();
    }

    @Override
    public void any(Augmentations augmentations) throws XNIException {
        getSerializer().emptyElement("any");
    }

    @Override
    public void empty(Augmentations augmentations) throws XNIException {
        getSerializer().emptyElement("empty");
    }

    @Override
    public void startGroup(Augmentations augmentations) throws XNIException {
        getSerializer().startElement("group");
    }

    @Override
    public void endGroup(Augmentations augmentations) throws XNIException {
        getSerializer().endElement();
    }

    @Override
    public void pcdata(Augmentations augmentations) throws XNIException {
        getSerializer().emptyElement("pcdata");
    }

    @Override
    public void element(String elementName, Augmentations augmentations)
        throws XNIException
    {
        final Serialization s = getSerializer();
        s.emptyElement("element");
        s.attribute("name", elementName);
    }

    @Override
    public void separator(short separator, Augmentations augmentations)
            throws XNIException
    {
        final Serialization s = getSerializer();
        s.emptyElement("sep");
        if (separator == 0)
            s.attribute("type", "|");
        else
            s.attribute("type", ",");
    }

    @Override
    public void occurrence(short occurrence, Augmentations augmentations)
            throws XNIException
    {
        final Serialization s = getSerializer();
        s.emptyElement("occur");
        if (occurrence == 2)
            s.attribute("type", "?");
        else if (occurrence == 3)
            s.attribute("type", "*");
        else
            s.attribute("type", "+");
    }

    protected void externalEntityLocation(final String resolved) {
        final XMLLocator loc = getLocator();
        if (loc != null) {
            final Serialization s = getSerializer();
            final String base = loc.getBaseSystemId();
            if (!base.equals(currentEntity)) {
                currentEntity = base;
                s.comment(" %s ", currentEntity);
            }
            s.comment(" %s " , resolved);
            s.comment(" Line %s %s ",
                      loc.getLineNumber(), loc.getBaseSystemId());
        }
    }

    protected void locationInformation() {
        final XMLLocator loc = getLocator();
        if (loc != null) {
            final Serialization s = getSerializer();
            final String base = loc.getBaseSystemId();
            if (base == null) {
                currentEntity = base;
                //l.rule();
                s.comment(" Base: %s " , loc.getBaseSystemId());
                s.comment(" Resolved: %s " , loc.getExpandedSystemId());
                s.comment(" System ID: %s " , loc.getLiteralSystemId());
                //l.rule();
                //l.outln("");
            } else if (!base.equals(currentEntity)) {
                currentEntity = base;
                //l.rule();
                s.comment(" %s " , loc.getBaseSystemId());
                //l.rule();
                //l.outln("");
            }
            s.comment(" Line %s %s ",
                      loc.getLineNumber(), loc.getBaseSystemId());
        }
    }

    public void ancestry(final String prefix) throws XNIException {
        final StringBuilder sb = new StringBuilder();
        sb.append(" " + prefix + ": [");
        boolean first = true;
        for (final String entityName : entityStack) {
            if (first) {
                first = false;
                sb.append(entityName);
            } else {
                sb.append(" / " + entityName);
            }
        }
        sb.append("] ");
        final Serialization s = getSerializer();
        s.comment(sb.toString());
        //l.outln("");
    }
}
