package com.kendallshaw.dtdnormalizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Stack;
import java.util.regex.Matcher;

import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;

import com.kendallshaw.dtdnormalizer.attributes.AttributeDeclaration;
import com.kendallshaw.dtdnormalizer.attributes.AttributeDeclarationWriter;

public class DtdSerialization extends SerializationMixin
    implements AttributeDeclarationWriter, Serialization
{

    private Writer writer;

    private XMLLocator locator;

    private boolean withComments = false;

    private Stack<String> entityStack = new Stack<String>();

    private boolean inExternalSubset = false;

    private boolean includingAll = false;

    private boolean include = false;

    private static final String WEIRD_SCHEME = "file:////";

    private static int WEIRD_SCHEME_LENGTH = WEIRD_SCHEME.length();

    public DtdSerialization() throws Exception {
        final PrintWriter w = new PrintWriter(System.out);
        setSerializationWriter(w);
    }

    public DtdSerialization(File f) throws Exception {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(f);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);
            setSerializationWriter(new PrintWriter(bw));
        } catch (Exception e) {
            if (fos != null)
                fos.close();
            throw e;
        }
    }

    public DtdSerialization(Writer w) throws Exception {
        setSerializationWriter(w);
    }

    public DtdSerialization(OutputStream os, String encoding) throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
        setSerializationWriter(osw);
    }

    @Override
    public Writer getSerializationWriter() {
        return writer;
    }

    @Override
    public void setSerializationWriter(Writer w) {
        writer = w;
    }

    @Override
    public XMLLocator getLocator() {
        return locator;
    }

    @Override
    public void setLocator(XMLLocator loc) {
        locator = loc;
    }

    public boolean isWithComments() {
        return withComments;
    }

    public void setWithComments(boolean c) {
        withComments = c;
    }

    @Override
    public boolean isIncludingAll() {
        return includingAll;
    }

    @Override
    public void setIncludingAll(boolean includingAll) {
        this.includingAll = includingAll;
    }

    @Override
    public void resetTargetResource(URI uri) throws Exception {
    }

    @Override
    public void startDocument(String root) throws XNIException {
        // ignore
    }

    @Override
    public void endDocument() throws XNIException {
        try {
            Writer w = getSerializationWriter();
            w.flush();
            w.close();
        } catch (Exception e) {
            throw new XNIException(e);
        }
    }

    @Override
    public void xmlDeclaration(String version, String encoding,
                               String standalone)
        throws XNIException
    {
        // ignored
    }

    @Override
    public void doctypeDeclaration(String root, String publicId,
                                   String systemId)
        throws XNIException
    {
        inExternalSubset = false;
        if (isWithComments()) {
            locationComment(publicId, systemId);
            out("<!-- DOCTYPE %s -->\n", root);
        }
    }

    @Override
    public void textDeclaration(String version, String encoding)
        throws XNIException
    {
        if (isWithComments()) {
            out("\n<!-- xml");
            if (version != null && !version.trim().isEmpty())
                out(" version=\"%s\"", version);
            if (encoding != null && !encoding.trim().isEmpty())
                out(" encoding=\"%s\"", encoding);
            out(" -->\n");
        }
    }

    @Override
    public void comment(String fmt, Object... args)
        throws XNIException
    {
        if (isWithComments()) {
            locationComment();
            if (args.length == 0)
                out("<!--%s-->\n", fmt);
            else
                out("<!--" + fmt + "-->\n", args);
        }

    }

    @Override
    public void processingIntruction(String target, XMLString data)
        throws XNIException
    {
        if (isWithComments())
            locationComment();
        if (data == null)
            out("<?%s?>\n", target);
        else
            out("<?%s %s?>\n", target, data);
    }

    @Override
    public void startConditionalSection(String condition)
        throws XNIException
    {
        if (isWithComments()) {
            locationComment();
            out("\n<!-- [%s[ -->\n", condition.toUpperCase());
        }
    }

    @Override
    public void endConditionalSection() {
        if (isWithComments()) {
            locationComment();
            out("<!-- ]] -->\n");
        }
    }

    @Override
    public void startExternalSubset() throws XNIException {
        inExternalSubset = true;
    }

    @Override
    public void endExternalSubset() throws XNIException {
        inExternalSubset = false;
    }
                                      
    @Override
    public void internalEntityDeclaration(String name, XMLString text,
                                          XMLString rawText,
                                          boolean includeOverride)
        throws XNIException
    {
        boolean parameterEntity = name.startsWith("%");
        String entityName = parameterEntity ? "% "
                          + name.substring(1) : name;
        String entityText = TextHandler.entityText(text, rawText);
        if (isWithComments())
            locationComment();
        if (!parameterEntity
            && (isIncludingAll() || includeOverride || !inExternalSubset))
        {
            String normalized = normalizedText(entityText);
            out(String.format("<!ENTITY %s \"%s\">\n",
                              name, normalized.replaceAll("\"", "&#x22;")));
        } else if (isWithComments()) {
            final String textString = text.toString();
            final String rawString = rawText.toString();
            out("<!-- ENTITY: %s", entityName);
            if (textString.equals(rawString)
                && textString.length() == rawString.length())
                out(" \"%s\" -->\n",
                    normalizedText(textString).replaceAll("\"", "&#x22;"));
            else if (textString.length() < 1)
                out(" \"%s\" -->\n",
                    normalizedText(rawString.replaceAll("\"", "&#x22;")));
            else {
                String cooked =
                    rawEntityText(textString).replaceAll("\"", "&#x22;");
                String raw =
                    rawEntityText(rawString).replaceAll("\"", "&#x22;");
                out(" \"%s\" -->\n<!--         [%s] -->\n",
                    normalizedText(cooked), normalizedText(raw));
            }
        }
    }

    @Override
    public void externalEntityDeclaration(String name, String publicId,
                                          String systemId)
        throws XNIException
    {
        if (isWithComments()) {
            locationComment(publicId, systemId);
            final String entityName = name.startsWith("%") ? "% "
                    + name.substring(1) : name;
            out("<!--ENTITY %s", entityName);
            if (publicId == null)
                out(" SYSTEM \"%s\"", systemId);
            else
                out(" PUBLIC \"%s\" \"%s\"", publicId, systemId);
            out("-->\n");
        }
    }

    @Override
    public void unparsedEntityDeclaration(String name,
                                          String publicId, String systemId,
                                          String notation)
        throws XNIException
    {
        if (isWithComments())
            locationComment();
        out("<!ENTITY %s", name);
        if (publicId == null)
            out(" SYSTEM \"%s\"", systemId.trim());
        else
            out(" PUBLIC \"%s\" \"%s\"", publicId.trim(), systemId.trim());
        out(" NDATA " + notation + ">\n");
    }

    @Override
    public void startEntity(String name) throws XNIException {
        entityStack.push(name);
        if (isWithComments()) {
            locationComment();
            out("<!-- start of entity %s -->\n", name);
        }
    }

    @Override
    public void endEntity() throws XNIException {
        if (isWithComments()) {
            locationComment();
            out("<!-- end of entity %s -->\n\n", entityStack.pop());
        }
    }

    @Override
    public void elementDeclaration(String name, String contentModel)
        throws XNIException
    {
        if (isWithComments())
            locationComment();
        String cm = contentModel.replace(",", ", ").replace("|", " | ");
        out("<!ELEMENT %s %s>\n", name, cm);
    }

    @Override
    public void startAttributeListDeclaration(String name)
        throws XNIException
    {
        if (isWithComments())
            locationComment();
        out("<!ATTLIST %s", name);
    }

    @Override
    public void endAttributeListDeclaration() throws XNIException {
        out(">\n");
    }

    @Override
    public void attributeDeclaration(String name, String type,
                                     String[] enumeration,
                                     String defaultType,
                                     XMLString defaultValue,
                                     XMLString rawDefaultValue)
        throws XNIException
    {
        out("\n          %s", name);
        if (enumeration == null) {
            if (CDATA == type) out(" %s", CDATA);
            else if (ID == type) out(" %s", ID);
            else if (IDREF == type) out(" %s", IDREF);
            else if (IDREFS == type) out(" %s", IDREFS);
            else if (ENTITY == type) out(" %s", ENTITY);
            else if (ENTITIES == type) out(" %s", ENTITIES);
            else if (NMTOKEN == type) out(" %s", NMTOKEN);
            else if (NMTOKENS == type) out(" %s", NMTOKENS);
        } else {
            if (NOTATION == type)
                out(" %s (", NOTATION);
            else
                out(" (");
            boolean first = true;
            for (final String e : enumeration) {
                if (first)
                    out(" %s", e);
                else
                    out(" | %s", e);
                first = false;
            }
            out(" )");
        }

        if (defaultValue == null) {
            out(" %s", defaultType);
        } else {
            String text = TextHandler.entityText(defaultValue, rawDefaultValue);
            if (FIXED == defaultType)
                out(" %s", FIXED);
            out(" \"%s\"", text.replaceAll("\"", "&#22;"));
        }
    }

    @Override
    public void notationDeclaration(String name,
                                    String publicId, String systemId)
        throws XNIException
    {
        if (isWithComments())
            locationComment();
        out("<!NOTATION %s", name);
        if (publicId == null)
            out(" SYSTEM \"%s\">\n", systemId);
        else if (systemId == null)
            out(" PUBLIC \"%s\">\n", publicId);
        else
            out(" PUBLIC \"%s\" \"%s\">\n", publicId, systemId);
            
    }

    @Override
    public void redefinition(String entityName) throws XNIException {
        if (isWithComments()) {
            locationComment();
            out("<!-- redefinition of %s -->\n", entityName);
        }
    }

    @Override
    public void startElement(String name) throws XNIException {
        // ignored
    }

    @Override
    public void endElement() throws XNIException {
        // ignored
    }

    @Override
    public void emptyElement(String name) throws XNIException {
        // ignored
    }

    @Override
    public void element(String name, String text) throws XNIException {
        // ignored
    }

    @Override
    public void attribute(String name, String text)
        throws XNIException
    {
        // ignored
    }

    @Override
    public void text(String text) throws XNIException {
        // ignored
    }

    @Override
    public void stackTrace(Exception e) throws XNIException {
        // ignored
    }

    @Override
    public void flush() throws XNIException {
        try {
            getSerializationWriter().flush();
        } catch (IOException e) {
            throw new XNIException(e);
        }
    }

    @Override
    public void writeAttributeDeclaration(AttributeDeclaration d)
        throws XNIException
    {
        // ignored
    }

    protected void locationComment() throws XNIException {
        locationComment(null, null);
    }

    protected void locationComment(String publicId, String systemId)
        throws XNIException
    {
        XMLLocator loc = getLocator();
        if (loc != null) {
            out("\n");
            String baseId = loc.getBaseSystemId();
            int lineNumber = loc.getLineNumber();
            if (systemId != null)
                externalIdentifier(publicId, systemId);
            if (baseId != null)  {
                String id = baseId;
                if (baseId.startsWith(WEIRD_SCHEME))
                    id = "file:/" + baseId.substring(WEIRD_SCHEME_LENGTH);
                out("<!-- %s", id);
                if(lineNumber < 0)
                    out(" -->\n");
                else
                    out(" [" + lineNumber + "] -->\n");
            }
        }
    }

    protected void externalIdentifier(final String p, final String s) {
        if (p != null && !"".equals(p))
            out("<!-- public-id: " + p + " -->\n");
        out("<!-- system-id: " + s + " -->\n");
    }

    protected void out(final String fmt, final Object... args)
        throws XNIException
    {
        try {
            Writer w = getSerializationWriter();
            if (args.length == 0)
                w.append(fmt);
            else
                w.append(String.format(fmt, args));
        } catch (IOException e) {
            throw new XNIException(e);
        }
        flush();
    }
}
