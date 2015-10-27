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
import org.w3c.dom.Document;

import com.kendallshaw.dtdnormalizer.CommandLine;

public class TestCommandLine extends Utils {

    public static void main(String[] args) throws Exception {
        String[] arghs = {
                "-sdtd",
                "src/test/resources/smorgasbord/smorga.xml",
                "/tmp/out.dtd"
        };
        CommandLine cl = new CommandLine();
        cl.execute(arghs);
    }

    @Test
    public void entitySets() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        String[] args = {
                "--catalogs",
                "src/test/resources/entities/catalog.xml"
                + ";src/test/resources/entities-catalog.xml",
                "--entities", "src/test/resources/entity-sets.cfg",
                "--serialization", "dtd",
                "src/test/resources/entities/test.xml"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.execute(args);
            ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
            CharBuffer cb = Charset.forName("UTF-8").decode(bb);
            StringReader sr = new StringReader(cb.toString());
            BufferedReader br = new BufferedReader(sr);
            Set<String> expected = new HashSet<String>();
            expected.add("<!ENTITY snook \"SNOOK\">");
            expected.add("<!ENTITY afr \"&#x1D51E;\">");
            expected.add("<!ENTITY Afr \"&#x1D504;\">");
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
                "--catalogs",
                "src/test/resources/entities/catalog.xml"
                + ";src/test/resources/entities-catalog.xml",
                "--entities", "src/test/resources/entity-sets.cfg",
                "--serialization", "dtd",
                "src/test/resources/entities/nested.xml"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.execute(args);
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
    public void nestedEntitySetsXml() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        String[] args = {
                "--catalogs",
                "src/test/resources/entities/catalog.xml"
                + ";src/test/resources/entities-catalog.xml",
                "--entities", "src/test/resources/entity-sets.cfg",
                "src/test/resources/entities/nested.xml"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.execute(args);
            Document doc = dom(baos.toByteArray());
            Assert.assertEquals(nodeText(doc, esubText("before")), "BEFORE");
            Assert.assertEquals(nodeText(doc, esubText("between")), "BETWEEN");
            Assert.assertEquals(nodeText(doc, esubText("after")), "AFTER");
            Assert.assertEquals(nodeText(doc, esubText("a")), "a");
            Assert.assertEquals(nodeText(doc, esubText("b")), "b");
            Assert.assertEquals(nodeText(doc, esubText("c")), "c");
            Assert.assertEquals(nodeText(doc, esubText("one")), "1");
            Assert.assertEquals(nodeText(doc, esubText("two")), "2");
            Assert.assertEquals(nodeText(doc, esubText("hundred")), "100");
            Assert.assertEquals(nodeText(doc, esubText("roman-one")), "i");
            Assert.assertEquals(nodeText(doc, esubText("roman-two")), "ii");
            Assert.assertEquals(nodeText(doc, esubText("roman-three")), "iii");
            /*
            Assert.assertEquals(nodeText(doc, esubText("text")),
                                "Abc & ("
                                + new String(Character.toChars(0x1D35))
                                + ") 123 &bork; xyz "
                                + new String(Character.toChars(0x1D35)));
            
            */
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
                "--catalogs",
                "src/test/resources/entities/catalog.xml"
                + ";src/test/resources/entities-catalog.xml",
                "--entities", "src/test/resources/doctype-entity.cfg",
                "--serialization", "dtd",
                "src/test/resources/entities/test.xml"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.execute(args);
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
                "--catalogs",
                "src/test/resources/entities/catalog.xml"
                + ";src/test/resources/entities-catalog.xml",
                "--entities", 
                "--serialization", "dtd",
                "src/test/resources/entities/test.xml"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.execute(args);
            testAllEntitiesXml(baos);
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
            cl.execute(args);
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

    @Test
    public void testUtf16BeBomDtd() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        String[] args = {
            "src/test/resources/characters/characters-16.xml",
            "-sdtd",
            "--entities"
        };
        CommandLine cl = new CommandLine();
        try {
            cl.execute(args);
            ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
            CharBuffer cb = Charset.forName("UTF-8").decode(bb);
            StringReader sr = new StringReader(cb.toString());
            BufferedReader br = new BufferedReader(sr);
            Set<String> expected = new HashSet<String>();
            expected.add("<!ENTITY right \""
                         + new String(Character.toChars(0xFFEB))
                         + "\">");
            expected.add("<!ENTITY right-hex \"&#xFFEB;\">");
            expected.add("<!ENTITY kana \""
                         + new String(Character.toChars(0x1B000))
                         + "\">");
            expected.add("<!ENTITY kana-hex \"&#x1B000;\">");
            expected.add("<!ENTITY a \""
                         + new String(Character.toChars(0x10300))
                         + "\">");
            expected.add("<!ENTITY a-hex \"&#x10300;\">");
            expected.add("<!ENTITY bar \""
                         + new String(Character.toChars(0x1D100))
                         + "\">");
            expected.add("<!ENTITY bar-hex \"&#x1D100;\">");
            expected.add("<!ENTITY coda \""
                         + new String(Character.toChars(0x1D10C))
                         + "\">");
            expected.add("<!ENTITY coda-hex \"&#x1D10C;\">");
            expected.add("<!ENTITY some \"right &right; hex &right-hex;"
                         + " and &a; and &a-hex; and code &coda; hex"
                         + " &coda-hex;\">");
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
        expected.add("<!ENTITY text \"Abc &amp; (&#x1D35;) 123 &bork; xyz "
                     + "&#x1D35;\">");
        expected.add("<!ENTITY afr \"&#x1D51E;\">");
        expected.add("<!ENTITY Afr \"&#x1D504;\">");
        expected.add("<!ENTITY fraktur-z \""
                     + new String(Character.toChars(0x1d59f))
                     + "\">");
        expected.add("<!ENTITY gcirc \"&#x0011D;\">");
        String line = br.readLine();
        while (line != null) {
            expected.remove(line);
            line = br.readLine();
        }
        Assert.assertEquals(0, expected.size());
    }

    private void testAllEntitiesXml(ByteArrayOutputStream baos)
        throws Exception
    {
        Document doc = dom(baos.toByteArray());
        /*
        Set<String> expected = new HashSet<String>();
        expected.add("<!ENTITY snook \"SNOOK\">");
        expected.add("<!ENTITY bork \"bork\">");
        expected.add("<!ENTITY asdf \"'\">");
        expected.add("<!ENTITY other \"a&asdf;\">");
        expected.add("<!ENTITY double-quote \"&#x22;\">");
        expected.add("<!ENTITY text \"Abc &amp; (&#x1D35;) 123 &bork; xyz "
                     + "&#x1D35;\">");
        expected.add("<!ENTITY afr \"&#x1D51E;\">");
        expected.add("<!ENTITY Afr \"&#x1D504;\">");
        expected.add("<!ENTITY fraktur-z \""
                     + new String(Character.toChars(0x1d59f))
                     + "\">");
        expected.add("<!ENTITY gcirc \"&#x0011D;\">");
        String line = br.readLine();
        while (line != null) {
            expected.remove(line);
            line = br.readLine();
        }
        Assert.assertEquals(0, expected.size());
        */
    }
}
