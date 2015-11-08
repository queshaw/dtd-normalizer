package com.kendallshaw.dtdnormalizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;

import com.ctc.wstx.stax.WstxOutputFactory;
import com.kendallshaw.dtdnormalizer.attributes.AttributeDeclaration;
import com.kendallshaw.dtdnormalizer.attributes.AttributeDeclarationWriter;
import com.kendallshaw.dtdnormalizer.attributes.AttributeEnumeration;
import com.kendallshaw.dtdnormalizer.attributes.DefaultDeclaration;
import com.kendallshaw.dtdnormalizer.attributes.DefaultType;

public class XmlSerialization
    extends SerializationMixin
    implements AttributeDeclarationWriter, Serialization
{
    private Writer serializationWriter;

    private XMLStreamWriter xmlWriter = null;

    private XMLLocator locator;

    private boolean beforeOpen = true;

    public XmlSerialization() throws Exception {
        final XMLOutputFactory of = WstxOutputFactory.newInstance();
        final PrintWriter w = new PrintWriter(System.out);
        setSerializationWriter(w);
        setXmlWriter(of.createXMLStreamWriter(w));
    }

    public XmlSerialization(final File f) throws Exception {
        final XMLOutputFactory of = WstxOutputFactory.newInstance();
        if (f == null) {
            final PrintWriter pw = new PrintWriter(System.out);
            setSerializationWriter(pw);
            setXmlWriter(of.createXMLStreamWriter(pw));
        } else {
            final FileOutputStream fis = new FileOutputStream(f);
            final OutputStreamWriter osw = new OutputStreamWriter(fis, "UTF-8");
            final BufferedWriter bw = new BufferedWriter(osw);
            setSerializationWriter(bw);
            setXmlWriter(of.createXMLStreamWriter(bw));
        }
    }

    public XmlSerialization(final Writer w) throws Exception {
        final XMLOutputFactory of = XMLOutputFactory.newInstance();
        setSerializationWriter(w);
        setXmlWriter(of.createXMLStreamWriter(w));
    }

    protected XmlSerialization(final XMLStreamWriter w) throws Exception {
        setXmlWriter(w);
    }

    public Writer getSerializationWriter() {
        return serializationWriter;
    }

    public void setSerializationWriter(final Writer w) {
        serializationWriter = w;
    }

    public XMLStreamWriter getXmlWriter() {
        return xmlWriter;
    }

    public void setXmlWriter(final XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Override
    public XMLLocator getLocator() {
        return locator;
    }

    @Override
    public void setLocator(final XMLLocator loc) {
        locator = loc;
    }

    @Override
    public boolean isIncludingAll() {
        return true;
    }

    @Override
    public void setIncludingAll(boolean f) {
        // ignore
    }

    // TODO fix encoding
    @Override
    public void resetTargetResource(URI uri)
        throws IOException, XMLStreamException
    {
    	if (!beforeOpen) {
            flush();
            OutputStream os = null;
            OutputStreamWriter osw = null;
            try {
                try {
                    getXmlWriter().close();
                    final XMLOutputFactory of = WstxOutputFactory.newInstance();
                    if (uri.getScheme().equals("file"))
                        os = new FileOutputStream(new File(uri));
                    else
                        os = uri.toURL().openConnection().getOutputStream();
                    osw = new OutputStreamWriter(os);
                    setXmlWriter(of.createXMLStreamWriter(osw));
                } catch (MalformedURLException e) {
                    if (os != null)
                        os.close(); // Not dead code
                    throw new RuntimeException(e);
                } catch (FactoryConfigurationError e) {
                    if (os != null)
                        os.close();
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    	}
    }

    @Override
    public void startDocument(final String root) throws XNIException {
        try {
        	beforeOpen = false;
            final XMLStreamWriter w = getXmlWriter();
            w.writeStartDocument();
            w.writeStartElement(root);
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    @Override
    public void endDocument() throws XNIException {
        try {
            final XMLStreamWriter w = getXmlWriter();
            w.writeEndElement();
            w.writeEndDocument();
            w.flush();
            w.close();
            final Writer sw = getSerializationWriter();
            if (sw != null)
                sw.close();
        } catch (IOException e) {
            throw new XNIException("Error closing Writer", e);
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    @Override
    public void xmlDeclaration(final String version, final String encoding,
                               final String standalone)
        throws XNIException
    {
        try {
            final XMLStreamWriter w = getXmlWriter();
            w.writeEmptyElement("xml-declaration");
            w.writeAttribute("version", version);
            if (encoding != null && !"".equals(encoding))
                w.writeAttribute("encoding", encoding);
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    @Override
    public void doctypeDeclaration(String root, String publicId,
                                   String systemId, String resourceId)
        throws XNIException
    {
        try {
            final XMLStreamWriter w = getXmlWriter();
            w.writeStartElement("doctype-declaration");
            w.writeAttribute("root", root);
            externalIdentifier(publicId, systemId, resourceId);
            w.writeEndElement();
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    @Override
    public void textDeclaration(final String version, final String encoding)
        throws XNIException
    {
    }

    @Override
    public void comment(final String fmt, final Object... args) throws XNIException {
    }

    @Override
    public void processingIntruction(final String target, final XMLString data)
        throws XNIException
    {
        startElement("processing-instruction");
        attribute("target", target);
        attribute("data", data.toString());
        locationElement();
        endElement();
    }

    @Override
    public void startConditionalSection(final String condition)
        throws XNIException
    {
        startElement("conditional-section");
        attribute("condition", condition);
        locationElement();
    }

    @Override
    public void endExternalSubset()
        throws XNIException
    {
        endElement();
    }

    @Override
    public void startExternalSubset()
        throws XNIException
    {
        startElement("external-subset");
        locationElement();
    }

    @Override
    public void endConditionalSection()
        throws XNIException
    {
        endElement();
    }

    @Override
    public void internalEntityDeclaration(final String name,
                                          final XMLString text,
                                          final XMLString rawText,
                                          boolean unused)
        throws XNIException
    {
        final boolean parameterEntity = name.startsWith("%");
        final String tagName =
            parameterEntity ? "parameter-entity-declaration"
            : "entity-declaration";
        final String entityName =
            parameterEntity ? "% " + name.substring(1) : name;
        final String textString = text.toString();
        final String rawString = rawText.toString();
        startElement(tagName);
        attribute("name", entityName);
        locationElement();
        if (textString.equals(rawString)) {
            element("text", normalizedText(textString));
        } else {
            element("text", normalizedText(textString));
            startElement("raw-text");
            boolean inEntityReference = false;
            String entityReference = null;
            for (final String t : entityText(rawString)) {
                if (";".equals(t) && entityReference != null) {
                    writeProcessingInstruction("entity",
                                               entityReference);
                    entityReference = null;
                    inEntityReference = false;
                } else if ("%".equals(t) || "&#".equals(t) || "&".equals(t)) {
                    inEntityReference = true;
                } else if (inEntityReference) {
                    entityReference = t;
                } else {
                    text(t);
                }
            }
            endElement();
        }
        endElement();
    }

    @Override
    public void externalEntityDeclaration(String name, String publicId,
                                          String systemId)
        throws XNIException
    {
        final boolean parameterEntity = name.startsWith("%");
        final String tagName =
            parameterEntity ? "parameter-entity-declaration"
            : "entity-declaration";
        final String entityName =
            parameterEntity ? "% " + name.substring(1) : name;
        final XMLStreamWriter w = getXmlWriter();
        try {
            w.writeStartElement(tagName);
            w.writeAttribute("name", entityName);
            locationElement(publicId, systemId);
            w.writeEndElement();
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    @Override
    public void unparsedEntityDeclaration(String name,
                                          String publicId, String systemId,
                                          String notation)
        throws XNIException
    {
        final XMLStreamWriter w = getXmlWriter();
        try {
            w.writeStartElement("unparsed-entity-declaration");
            w.writeAttribute("name", name);
            w.writeAttribute("ndata", notation);
            locationElement(publicId, systemId);
            w.writeEndElement();
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
        
    }

    @Override
    public void startEntity(String name)
        throws XNIException
    {
        startElement("entity");
        attribute("name", name);
        locationElement();
    }

    @Override
    public void endEntity()
        throws XNIException
    {
        endElement();
    }

    @Override
    public void elementDeclaration(String name, String contentModel)
        throws XNIException
    {
    	startElement("element-declaration");
        attribute("name", name);
        locationElement();
        element("content-model", contentModel);
        endElement();
    }

    @Override
    public void startAttributeListDeclaration(String name) throws XNIException {
        startElement("attribute-list-declaration");
        attribute("name", name);
    }

    @Override
    public void endAttributeListDeclaration() throws XNIException {
        endElement();
    }

    @Override
    public void attributeDeclaration(String name, String type,
                                     String[] enumeration, String defaultType,
                                     XMLString defaultValue,
                                     XMLString rawDefaultValue)
            throws XNIException
    {
        final String value =
                defaultValue == null ? null : defaultValue.toString();
        writeAttributeDeclaration(new AttributeDeclaration(name,
                                                           type,
                                                           enumeration,
                                                           defaultType,
                                                           value));
    }

    @Override
    public void notationDeclaration(String name,
                                    String publicId, String systemId)
        throws XNIException
    {
    	startElement("notation");
        attribute("name", name);
        locationElement(publicId, systemId);
        endElement();
        
    }

    @Override
    public void redefinition(String entityName) throws XNIException {
        startElement("redefinition");
        attribute("name", entityName);
        locationElement();
        endElement();
    }

    public void startCdata(String text) throws XNIException {
        try {
            XMLStreamWriter w = getXmlWriter();
            w.writeCData(text);
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    public void startElement(final String name) throws XNIException {
        try {
            final XMLStreamWriter w = getXmlWriter();
            w.writeStartElement(name);
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    public void endElement() throws XNIException {
        try {
            final XMLStreamWriter w = getXmlWriter();
            w.writeEndElement();
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    public void emptyElement(final String name) throws XNIException {
        try {
            final XMLStreamWriter w = getXmlWriter();
            w.writeEmptyElement(name);
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    public void element(final String name, final String text)
        throws XNIException
    {
        try {
            final XMLStreamWriter w = getXmlWriter();
            w.writeStartElement(name);
            w.writeCharacters(text);
            w.writeEndElement();
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    public void attribute(final String name, final String text)
        throws XNIException
    {
        if (text != null) {
            try {
                final XMLStreamWriter w = getXmlWriter();
                w.writeAttribute(name, text);
            } catch (XMLStreamException e) {
                throw new XNIException(e);
            }
        }
    }

    public void text(final String text)
        throws XNIException
    {
        try {
            final String stripped = text.replaceAll("[ \t\n][ \t\n]+", " ");
            final XMLStreamWriter w = getXmlWriter();
            w.writeCharacters(stripped);
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    public void flush()
        throws XNIException
    {
        try {
            getXmlWriter().flush();
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    public void stackTrace(final Exception e) {
        startElement("stack-trace");
        element("message", e.getLocalizedMessage());
        for (final StackTraceElement se : e.getStackTrace()) {
            startElement("frame");
            element("file", "" + se.getFileName());
            element("class", se.getClassName());
            element("method", se.getMethodName());
            element("line-number", "" + se.getLineNumber());
            endElement();
        }
    }

    @Override
    public void writeAttributeDeclaration(AttributeDeclaration d)
            throws XNIException
    {
        final XMLStreamWriter w = getXmlWriter();
        try {
            w.writeStartElement("attribute-declaration");
            w.writeAttribute("name", d.name());
            switch (d.attributeType()) {
            case STRING:
                w.writeStartElement("string");
                break;
            case TOKEN:
                w.writeStartElement("token");
                w.writeAttribute("type", d.tokenType().toString());
                break;
            default:
                final AttributeEnumeration en = d.enumeration();
                startElement(en.isNotation() ? "notation" : "enumeration");
                for (final String t : en) {
                    element("entry", t);
                }
            }
            final DefaultDeclaration dd = d.defaultDeclaration();
            final DefaultType dt = dd.type();
            if (dt == null) {
                if (dd.fixed() == null)
                    element("default-value", dd.value());
                else {
                    startElement("default-value");
                    attribute("fixed", "true");
                    text(dd.value());
                    endElement();
                }
            } else if (DefaultType.IMPLIED == dt)
                emptyElement("implied");
            else if (DefaultType.REQUIRED == dt) {
                final String value = dd.value();
                if (value == null)
                    emptyElement("required");
                else {
                    startElement("default-value");
                    attribute("type", "required");
                    text(value);
                    endElement();
                }
            }
            w.writeEndElement();
            w.writeEndElement();
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    public void locationElement() throws XNIException {
        locationElement(null, null);
    }

    public void locationElement(final String publicId,
                                final String systemId)
        throws XNIException
    {
        final XMLLocator loc = getLocator();
        if (loc != null) {
            if (systemId == null)
                emptyElement("location");
            else {
                startElement("location");
            }
            attribute("href", loc.getBaseSystemId());
            int lineNumber = loc.getLineNumber();
            if (lineNumber > -1)
                attribute("line", Integer.toString(lineNumber));
            if (systemId != null) {
                externalIdentifier(publicId, systemId);
                endElement();
            }
        }
    }

    protected void writeProcessingInstruction(final String target,
                                              final String data)
        throws XNIException
    {
        try {
            final XMLStreamWriter w = getXmlWriter();
            if (data == null || "".equals(data))
                w.writeProcessingInstruction(target);
            else
                w.writeProcessingInstruction(target, data);
        } catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    private String writeProcessingInstructionText(final String target,
                                              final String data)
        throws XNIException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<?");
        if (data == null || "".equals(data))
            sb.append(target);
        else
            sb.append(target + " " + data);
        sb.append("?>");
        return sb.toString();
    }

    protected void externalIdentifier(final String p, final String s)
        throws XNIException
    {
        externalIdentifier(p, s, null);
    }

    protected void externalIdentifier(final String p, final String s,
                                      final String r)
        throws XNIException
    {
        if (p != null && !"".equals(p.trim()))
            element("public-id", p);
        if (s != null && !"".equals(s.trim()))
            element("system-id", s);
        if (r != null && !"".equals(r.trim()))
            element("resource-id", r);
    }
}
