package ks.dtdnormalizer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.kendallshaw.dtdnormalizer.CommandLine;
import com.kendallshaw.dtdnormalizer.OldCommandLine;

public class TestCommandLine {

    @Test
    public void entitySets() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        String[] args = {
                "--catalog",
                "src/test/resources/entities/catalog.xml"
                + ";src/test/resources/entities-catalog.xml",
                "--entities", "src/test/resources/entity-sets.cfg",
                "--serialization", "dtd",
                "src/test/resources/entities/test.xml"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.normalize(args);
            ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
            CharBuffer cb = Charset.forName("UTF-8").decode(bb);
            StringReader sr = new StringReader(cb.toString());
            BufferedReader br = new BufferedReader(sr);
            Set<String> expected = new HashSet<String>();
            expected.add("<!ENTITY afr \""
                         + new String(Character.toChars(0x1d51e))
                         + "\">");
            expected.add("<!ENTITY Afr \""
                         + new String(Character.toChars(0x1d504))
                         + "\">");
            expected.add("<!ENTITY fraktur-z \""
                         + new String(Character.toChars(0x1d59f))
                         + "\">");
            String line = br.readLine();
            while (line != null) {
                expected.remove(line);
                line = br.readLine();
            }
            Assert.assertEquals(0, expected.size());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setOut(stdout);
        }
    }

    @Test
    public void nestedEntitySets() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        String[] args = {
                "--catalog",
                "src/test/resources/entities/catalog.xml"
                + ";src/test/resources/entities-catalog.xml",
                "--entities", "src/test/resources/entity-sets.cfg",
                "--serialization", "dtd",
                "src/test/resources/entities/nested.xml"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.normalize(args);
            ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
            CharBuffer cb = Charset.forName("UTF-8").decode(bb);
            StringReader sr = new StringReader(cb.toString());
            BufferedReader br = new BufferedReader(sr);
            Set<String> expected = new HashSet<String>();
            expected.add("<!ENTITY before \"BEFORE\">");
            expected.add("<!ENTITY between \"BETWEEN\">");
            expected.add("<!ENTITY after \"AFTER\">");
            expected.add("<!ENTITY a \"a\">");
            expected.add("<!ENTITY b \"b\">");
            expected.add("<!ENTITY c \"c\">");
            expected.add("<!ENTITY one \"1\">");
            expected.add("<!ENTITY two \"2\">");
            expected.add("<!ENTITY hundred \"100\">");
            expected.add("<!ENTITY roman-one \"i\">");
            expected.add("<!ENTITY roman-two \"ii\">");
            expected.add("<!ENTITY roman-three \"iii\">");
            String line = br.readLine();
            while (line != null) {
                expected.remove(line);
                line = br.readLine();
            }
            Assert.assertEquals(0, expected.size());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setOut(stdout);
        }
    }

    @Test
    public void allEntitiesByDoctype() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        String[] args = {
                "--catalog",
                "src/test/resources/entities/catalog.xml"
                + ";src/test/resources/entities-catalog.xml",
                "--entities", "src/test/resources/doctype-entity.cfg",
                "--serialization", "dtd",
                "src/test/resources/entities/test.xml"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.normalize(args);
            testAllEntities(baos);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setOut(stdout);
        }
    }

    @Test
    public void allEntitiesOption () {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        String[] args = {
                "--catalog",
                "src/test/resources/entities/catalog.xml"
                + ";src/test/resources/entities-catalog.xml",
                "--entities", 
                "--serialization", "dtd",
                "src/test/resources/entities/test.xml"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.normalize(args);
            testAllEntities(baos);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setOut(stdout);
        }
    }

    @Test
    public void unparsedEntities() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        String[] args = {
                "src/test/resources/notation-subset.xml",
                "-sdtd"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.normalize(args);
            ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
            CharBuffer cb = Charset.forName("UTF-8").decode(bb);
            StringReader sr = new StringReader(cb.toString());
            BufferedReader br = new BufferedReader(sr);
            Set<String> expected = new HashSet<String>();
            expected.add("<!ENTITY a.jpg SYSTEM \"a.jpg\" NDATA jpeg>");
            expected.add("<!ENTITY b.jpg SYSTEM \"b.jpg\" NDATA jpeg>");
            expected.add("<!ENTITY a.png SYSTEM \"a.png\" NDATA png>");
            expected.add("<!NOTATION jpeg PUBLIC \"-//MEDIA JPEG//EN\">");
            expected.add("<!NOTATION png PUBLIC \"-//MEDIA PNG//EN\">");
            expected.add("          data NOTATION ( jpeg ) #REQUIRED>");
            expected.add("          data NOTATION ( jpeg | png ) #REQUIRED>");
            String line = br.readLine();
            while (line != null) {
                expected.remove(line);
                line = br.readLine();
            }
            Assert.assertEquals(0, expected.size());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setOut(stdout);
        }
    }

    private void testAllEntities(ByteArrayOutputStream baos) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
        CharBuffer cb = Charset.forName("UTF-8").decode(bb);
        StringReader sr = new StringReader(cb.toString());
        BufferedReader br = new BufferedReader(sr);
        Set<String> expected = new HashSet<String>();
        expected.add("<!ENTITY snook \"SNOOK\">");
        expected.add("<!ENTITY bork \"bork\">");
        expected.add("<!ENTITY asdf \"'\">");
        expected.add("<!ENTITY other \"a&asdf;\">");
        expected.add("<!ENTITY double-quote \"&#x22;\">");
        expected.add("<!ENTITY text \"Abc &amp; ("
                     + new String(Character.toChars(0x1d35))
                     + ") 123 &bork; xyz "
                     + new String(Character.toChars(0x1d35))
                     + "\">");
        expected.add("<!ENTITY afr \""
                     + new String(Character.toChars(0x1d51e))
                     + "\">");
        expected.add("<!ENTITY Afr \""
                     + new String(Character.toChars(0x1d504))
                     + "\">");
        expected.add("<!ENTITY fraktur-z \""
                     + new String(Character.toChars(0x1d59f))
                     + "\">");
        expected.add("<!ENTITY gcirc \""
                     + new String(Character.toChars(0x11d))
                     + "\">");
        String line = br.readLine();
        while (line != null) {
            expected.remove(line);
            line = br.readLine();
        }
        Assert.assertEquals(0, expected.size());
    }
}
