package ks.dtdnormalizer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.kendallshaw.dtdnormalizer.Constants;
import com.kendallshaw.dtdnormalizer.DtdNormalizer;
import com.kendallshaw.dtdnormalizer.DtdSerialization;
import com.kendallshaw.dtdnormalizer.ErrorHandler;
import com.kendallshaw.dtdnormalizer.IdentifierResolver;
import com.kendallshaw.dtdnormalizer.OutputIdentifier;
import com.kendallshaw.dtdnormalizer.Serialization;
import com.kendallshaw.dtdnormalizer.XmlSerialization;
import com.kendallshaw.dtdnormalizer.XniConfiguration;

public class Utils {

    public static final String ISUBSET = "/document-type";

    public static final String ESUBSET = 
        "/document-type/external-subset";

    public void normalizerIO(String inPath, String outPath,
                             String serializationName)
        throws Exception
    {
        InputSource is =
            "-".equals(inPath) ? stdIn() : fileInput(new File(inPath));
        OutputIdentifier out =
            "-".equals(outPath) ? stdOut() : fileOutput(new File(outPath));
        normalizerIO(is, out, serializationName);
    }

    public void normalizerIO(InputStream is, String inSystem,
                             OutputStream os, String outSystem,
                             String serializationName)
        throws Exception
    {
        normalizerIO(streamInput(is, inSystem),
                     streamOutput(os, outSystem),
                     serializationName);
    }

    public void normalizerIO(File in, File out, String serializationName)
        throws Exception
    {
        normalizerIO(fileInput(in),
                     fileOutput(out),
                     serializationName);
    }

    public void normalizerIO(InputSource in, OutputIdentifier out,
                             String serializationName)
        throws Exception
    {
        DtdNormalizer dn = new DtdNormalizer(new XniConfiguration());
        dn.setInput(in);
        dn.setOutput(out);
        OutputStreamWriter osw =
            new OutputStreamWriter(out.getByteStream(),
                                   out.getEncoding());
        Serialization ser = null;
        if (serializationName == null || "xml".equals(serializationName))
            ser = new XmlSerialization(osw);
        else
            ser = new DtdSerialization(osw);
        dn.setSerialization(ser);
        dn.setErrorHandler(new ErrorHandler(ser));
        dn.setIdentifierResolver(new IdentifierResolver());
        dn.serialize();
    }

    public InputSource byteInput(byte[] bytes)
        throws Exception
    {
        ByteArrayInputStream bais =
            new ByteArrayInputStream(bytes);
        InputSource is = new InputSource(bais);
        is.setSystemId("urn:os:stream:in");
        return is;
    }

    public InputSource fileInput(File f)
        throws Exception
    {
        FileInputStream fis = new FileInputStream(f);
        InputSource is = new InputSource(fis);
        is.setSystemId(f.toURI().toString());
        is.setEncoding("UTF-8");
        return is;
    }

    public InputSource streamInput(InputStream input, String systemId)
        throws Exception
    {
        InputSource is = new InputSource(input);
        is.setSystemId(systemId);
        is.setEncoding("UTF-8");
        return is;
    }

    public OutputIdentifier streamOutput(OutputStream output,
                                                   String systemId)
        throws Exception
    {
        OutputIdentifier oi = new OutputIdentifier(output,
                                                   "UTF-8",
                                                   systemId);
        return oi;
    }

    public InputSource textInput(String text)
        throws Exception
    {
        StringReader sr = new StringReader(text);
        InputSource is = new InputSource(sr);
        is.setSystemId("urn:os:stream:in");
        return is;
    }

    public InputSource stdIn()
        throws Exception
    {
        InputSource is = new InputSource(System.in);
        is.setSystemId(Constants.C_STDIN.toString());
        is.setEncoding("UTF-8");
        return is;
    }

    public OutputIdentifier stdOut()
        throws Exception
    {
        return new OutputIdentifier(System.out,
                                    "UTF-8",
                                    Constants.C_STDOUT.toString());
    }

    public OutputIdentifier fileOutput(File f)
        throws Exception
    {
        f.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(f);
        return new OutputIdentifier(fos,
                                   "UTF-8",
                                   f.toURI().toASCIIString());
    }

    public void assertEqualFiles(File a, File b)
        throws Exception
    {
        byte[] aBytes = fileBytes(a);
        byte[] bBytes = fileBytes(b);
        Assert.assertTrue(a + " and " + b + " differ.",
                          Arrays.equals(aBytes, bBytes));
    }

    public void assertSimpleXml(byte[] bytes)
        throws Exception
    {
        assertMatch("DTD has no root.",
                    byteInput(bytes),
                    "/document-type");
        assertMatch("Doctype root should be doc.",
                    byteInput(bytes),
                    "/*/doctype-declaration[@root='doc']");
        assertMatch("The internal subset should declare an entity.",
                    byteInput(bytes),
                    "/*/entity-declaration[@name='asdf']");
        assertMatch("There should be a doc element declaration.",
                    byteInput(bytes),
                    "/*/external-subset/element-declaration[@name='doc']");
    }

    public String nodeText(Document doc, String expression)
        throws Exception
    {
        XPathFactory xf = XPathFactory.newInstance();
        XPath xpath = xf.newXPath();
        return (String) xpath.evaluate(expression, doc, XPathConstants.STRING);
    }

    public String esubRawText(String name) {
        return String.format("%s//%s",
                             ESUBSET,
                             entityRawText(name));
    }

    public String esubText(String name) {
        return String.format("%s//%s",
                             ESUBSET,
                             entityText(name));
    }

    public String isubText(String name) {
        return String.format("%s/%s",
                             ISUBSET,
                             entityText(name));
    }

    public String entityText(String name) {
        return String.format("entity-declaration[@name=\'%s\']/text", name);
    }

    public String entityRawText(String name) {
        return String.format("entity-declaration[@name=\'%s\']/raw-text", name);
    }

    public void assertMatch(String msg, InputSource is, String expression)
        throws Exception
    {
        XPathFactory xf = XPathFactory.newInstance();
        XPath xpath = xf.newXPath();
        boolean f =
            (Boolean) xpath.evaluate(expression, is, XPathConstants.BOOLEAN);
        Assert.assertTrue(msg, f);
    }

    public void assertMatch(String msg, Document doc, String expression)
        throws Exception
    {
        XPathFactory xf = XPathFactory.newInstance();
        XPath xpath = xf.newXPath();
        boolean f =
            (Boolean) xpath.evaluate(expression, doc, XPathConstants.BOOLEAN);
        Assert.assertTrue(msg, f);
    }

    public byte[] fileBytes(File f) throws Exception {
        FileInputStream fis = new FileInputStream(f);
        FileChannel fch = fis.getChannel();
        int length = Long.valueOf(f.length()).intValue();
        ByteBuffer bb = 
            ByteBuffer.allocate(length);
        while (true) {
            length -= fch.read(bb);
            if (length < 1)
                break;
        }
        byte[] bytes = bb.array();
        fch.close();
        return bytes;
    }

    public boolean entityTextEquals(String expected,
                                    Document doc, String exp)
        throws Exception
    {
        return expected.equals(nodeText(doc, exp));
    }

    public boolean textEquals(String expected,
                              byte[] bytes, String charset)
        throws Exception
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        CharBuffer cb = Charset.forName(charset).decode(bb);
        return expected.equals(cb.toString());
    }

    public Document dom(byte[] bytes) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(byteInput(bytes));
    }
}
