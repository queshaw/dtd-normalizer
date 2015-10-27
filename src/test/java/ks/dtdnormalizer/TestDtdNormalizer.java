package ks.dtdnormalizer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.parsers.SAXParser;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.kendallshaw.dtdnormalizer.CommandLine;
import com.kendallshaw.dtdnormalizer.Constants;
import com.kendallshaw.dtdnormalizer.DtdNormalizer;
import com.kendallshaw.dtdnormalizer.DtdSerialization;
import com.kendallshaw.dtdnormalizer.ErrorHandler;
import com.kendallshaw.dtdnormalizer.OutputIdentifier;
import com.kendallshaw.dtdnormalizer.Serialization;
import com.kendallshaw.dtdnormalizer.XmlSerialization;
import com.kendallshaw.dtdnormalizer.XniConfiguration;

public class TestDtdNormalizer extends Utils {

    public static void main(String[] args) throws Exception {
//        "--entities", "src/test/resources/entity-sets.cfg",

        String[] arghs = {
                "-Dxml.catalog.verbosity=9",
                "-C/home/kshaw/opt/DITA-OT2.0/catalog-dita.xml",
                "--serialization", "dtd",
                "/home/kshaw/projects/relaxng/concept.xml",
                "/tmp/out.dtd"
        };
//        String[] arghs = {
//          "src/test/resources/content-models/groups.xml",
//          "-sdtd"
//        };
        CommandLine cl = new CommandLine();
        cl.execute(arghs);
    }

    @Test
    public void inputFileAndStdout() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        File f = new File("src/test/resources/simple.xml");
        try {
            normalizerIO(fileInput(f), stdOut(), "xml");
            assertSimpleXml(baos.toByteArray());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setOut(stdout);
        }
    }

    @Test
    public void inputFileAndOutputFile() {
        File f = new File("src/test/resources/simple.xml");
        File fo = new File("tmp/simple-out.xml");
        fo.getParentFile().mkdirs();
        try {
            normalizerIO(fileInput(f), fileOutput(fo), "xml");
            assertSimpleXml(fileBytes(fo));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
        }
    }

    @Test
    public void inputFileAndOutputDtdFile() {
        File f = new File("src/test/resources/simple.xml");
        File expected = new File("src/test/resources/simple-expected.dtd");
        File fo = new File("tmp/simple-out.dtd");
        try {
            normalizerIO(fileInput(f), fileOutput(fo), "dtd");
            assertEqualFiles(expected, fo);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
        }
    }

    @Test
    public void catalogResolves() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        String[] args = {
                "--catalogs", "src/test/resources/conditional/catalog.xml",
                "src/test/resources/conditional.xml"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.execute(args);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setOut(stdout);
        }
    }
}
