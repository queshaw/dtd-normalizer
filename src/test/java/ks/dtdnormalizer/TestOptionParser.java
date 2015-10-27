package ks.dtdnormalizer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.kendallshaw.dtdnormalizer.OptionParser;

public class TestOptionParser {

    public static void main(String[] args) throws Exception {
        OptionParser op = new OptionParser();
        op.parseCommandLine(new String[] {
                "--help", "--",
                "src/test/resources/simple.xml"
            });
        System.out.println("entities=" + op.getEntitiesPath());
        System.out.println("includingAll=" + op.isIncludingAll());
        System.out.println("files=" + op.getInputPath());
    }

    @Test
    public void existingInputFileOnly() {
        String inputFile = "src/test/resources/topic.xml";
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(new String[] {
                    inputFile
                });
        String path = op.getInputPath();
        Assert.assertNotNull(path);
    }

    @Test
    public void plausibleOutputFile() {
        String inputFile = "src/test/resources/topic.xml";
        String outputFile = "src/test/resources/out.xml";
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(new String[] {
                    inputFile,
                    outputFile
                });
        String path = op.getInputPath();
        String path2 = op.getOutputPath();
        Assert.assertNotNull(path);
        Assert.assertNotNull(path2);
    }

    @Test
    public void implausibleOutputFile() {
        PrintStream stderr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setErr(ps);;
        try {
            String inputFile = "src/test/resources/topic.xml";
            String outputFile = "src/test/reszources/out.xml";
            new OptionParser().parseCommandLineWithoutExit(new String[] {
                    inputFile,
                    outputFile
                });
            Assert.fail("An exception should have been thrown.");
        } catch (OptionParser.OptionParseError e) {
        } finally {
            System.setErr(stderr);
        }
    }

    @Test
    public void validComments() {
        String inputFile = "src/test/resources/topic.xml";
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(new String[] {
                    "-c", "yes",
                    inputFile
                });
        boolean c = op.isWithComments();
        Assert.assertTrue(c);
    }

    @Test
    public void emptyComments() {
        String inputFile = "src/test/resources/topic.xml";
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(new String[] {
                    "-c", "--",
                    inputFile
                });
        boolean c = op.isWithComments();
        Assert.assertTrue(c);
    }

    @Test
    public void invalidComments() {
        PrintStream err = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setErr(ps);
        try {
            String inputFile = "src/test/resources/topic.xml";
            OptionParser op =
                new OptionParser().parseCommandLineWithoutExit(new String[] {
                        "-c", "yup",
                        inputFile
                    });
            boolean c = op.isWithComments();
            Assert.assertTrue(c);
            Assert.fail("An exception should have been thrown.");
        } catch (OptionParser.OptionParseError e) {
        } finally {
            System.setErr(err);
        }
        System.setErr(err);
    }

    @Test
    public void squishedOption() {
        String inputFile = "src/test/resources/topic.xml";
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(new String[] {
                    "-cyes",
                    inputFile
                });
        boolean c = op.isWithComments();
        Assert.assertTrue(c);
    }

    @Test
    public void longOption() {
        String inputFile = "src/test/resources/topic.xml";
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(new String[] {
                    "--comment=yes",
                    inputFile
                });
        boolean c = op.isWithComments();
        Assert.assertTrue(c);
    }

    @Test
    public void spaceSeparator() {
        String inputFile = "src/test/resources/topic.xml";
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(new String[] {
                    "--comment", "yes",
                    inputFile
                });
        boolean c = op.isWithComments();
        Assert.assertTrue(c);
    }

    @Test
    public void specifiedProperties() {
        String inputFile = "src/test/resources/topic.xml";
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(new String[] {
                    "-Ddtd-normalizer.comments=yes",
                    inputFile
                });
        boolean c = op.isWithComments();
        Assert.assertTrue(c);
    }

    @Test
    public void unspecifiedProperties() {
        System.setProperty("dtd-normalizer.comments", "yes");
        try {
            String inputFile = "src/test/resources/topic.xml";
            OptionParser op =
                new OptionParser().parseCommandLineWithoutExit(new String[] {
                        inputFile
                    });
            boolean c = op.isWithComments();
            Assert.assertTrue(c);
        } finally {
            System.clearProperty("dtd-normalizer.comments");
        }
    }

    @Test
    public void validCharset() {
        String[] args = new String[] {
            "src/test/resources/test.xml",
            "--charset=UTF-16"
        };
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(args);
        Charset cs = op.getCharset();
        Assert.assertNotNull(cs);
        Assert.assertEquals("UTF-16", cs.name());
    }

    @Test
    public void invalidCharset() {
        PrintStream stderr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baos));
        String[] args = new String[] {
            "src/test/resources/test.xml",
            "--charset=boink"
        };
        try {
            OptionParser op =
                new OptionParser().parseCommandLineWithoutExit(args);
            op.getCharset();
            Assert.fail("Should have failed.");
        } catch (Exception e) {
        }
        System.setErr(stderr);
    }

    @Test
    public void charsetsOptionRecognized() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        String[] args = new String[] {
            "--charsets"
        };
        try {
            new OptionParser().parseCommandLineWithoutExit(args);
        } catch (Exception e) {
        } finally {
            System.setOut(stdout);
        }
        try {
            ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
            CharBuffer cb = Charset.forName("UTF-8").decode(bb);
            StringReader sr = new StringReader(cb.toString());
            BufferedReader br = new BufferedReader(sr);
            String line = br.readLine();
            boolean found = false;
            while (line != null) {
                line = br.readLine();
                if (line.startsWith("IANA Registered:")) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found);
            br.close();
            sr.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void reportNoArgs() {
        testReport("", true, true, true, true);
    }

    @Test
    public void reportCatalogsAll() {
        testReport("catalogs,all", true, true, true, true);
    }
    @Test
    public void reportCharsetsAll() {
        testReport("charsets,all", true, true, true, true);
    }

    @Test
    public void reportEntitesAll() {
        testReport("entities,all", true, true, true, true);
    }

    @Test
    public void reportCatalogsCharsestsAll() {
        testReport("catalogs,charsets,all", true, true, true, true);
    }

    @Test
    public void reportEntitiesAll() {
        testReport("catalogs,entities,all", true, true, true, true);
    }

    @Test
    public void reportCharsetsEntitiesAll() {
        testReport("charsets,entities,all", true, true, true, true);
    }

    @Test
    public void reportCatalogs() {
        testReport("catalogs", true, true, false, false);
    }

    @Test
    public void reportCharsets() {
        testReport("charsets", true, false, true, false);
    }

    @Test
    public void reportEntities() {
        testReport("entities", true, false, false, true);
    }

    @Test
    public void reportCatalogsCharsets() {
        testReport("catalogs,charsets", true, true, true, false);
    }

    @Test
    public void reportCatalogsEntities() {
        testReport("catalogs,entities", true, true, false, true);
    }

    @Test
    public void reportCharsetsEntities() {
        testReport("charsets,entities", true, false, true, true);
    }

    @Test
    public void reportCatalogsCharsetsEntities() {
        testReport("catalogs,charsets,entities", true, true, true, true);
    }

    @Test
    public void normalizedCatalogList() {
        String[] args = new String[] {
            "src/test/resources/test.xml",
            "--catalogs=src/test/resources/entities-catalog.xml"
            + ";src/test/rsources/entities/catalog.xml"
        };
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(args);
    }

    @Test
    public void validEntities() {
        String inputFile = "src/test/resources/topic.xml";
        String entitiesPath = "src/test/resources/entity-sets.cfg";
        FileInputStream fis = null;
        try {
            File entitiesFile = new File(entitiesPath);
            String fullPath = entitiesFile.getCanonicalPath();
            fis = new FileInputStream(entitiesFile);
            Properties props = new Properties();
            props.setProperty("-//KS//ENTITIES Unicode//EN", "public");
            props.setProperty("urn:ks:entities:other", "system");
            OptionParser op =
                new OptionParser().parseCommandLineWithoutExit(new String[] {
                        "--entities", entitiesPath,
                        inputFile
                    });
            String value = op.getEntitiesPath();
            Assert.assertNotNull("Missing entity path.", value);
            Assert.assertEquals("Entity path differs.", fullPath, value);
            Map<String, String> map = op.inclusionIds();
            Assert.assertNotNull("Missing inclusion map.", map);

            Enumeration<Object> en = props.keys();
            while (en.hasMoreElements()) {
                String propKey = (String) en.nextElement();
                String propValue = props.getProperty(propKey);
                String mapValue = map.get(propKey);
                Assert.assertNotNull("Missing map key " + propKey + ".",
                                     mapValue);
                Assert.assertEquals("Values differ for map key "
                                    + propKey + ".", propValue, mapValue);
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void allEntities() {
        String inputFile = "src/test/resources/topic.xml";
        FileInputStream fis = null;
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(new String[] {
                    "--entities", "--",
                    inputFile
                });
        Assert.assertTrue("Include all should be true.",
                          op.isIncludingAll());
    }

    @Test
    public void invalidEntities() {
        PrintStream err = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setErr(ps);
        String inputFile = "src/test/resources/topic.xml";
        String entitiesPath = "src/test/resources/invalid-entity-sets.cfg";
        FileInputStream fis = null;
        try {
            File entitiesFile = new File(entitiesPath);
            String fullPath = entitiesFile.getCanonicalPath();
            fis = new FileInputStream(entitiesFile);
            OptionParser op =
                new OptionParser().parseCommandLineWithoutExit(new String[] {
                        "--entities", entitiesPath,
                        inputFile
                    });
            Assert.fail(entitiesPath + " should have caused an error.");
        } catch (OptionParser.OptionParseError e) {
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setErr(err);
        }
        System.setErr(err);
    }

    private void testReport(String arg,
                            boolean reporting,
                            boolean catalogs,
                            boolean charsets,
                            boolean entities)
    {
        String optArg = "".equals(arg) ? "" : ("=" + arg);
        String[] args = new String[] {
            "src/test/resources/test.xml",
            "--report" + optArg
        };
        OptionParser op =
            new OptionParser().parseCommandLineWithoutExit(args);
        if (reporting)
            Assert.assertTrue(op.isReporting());
        else
            Assert.assertFalse(op.isReporting());
        if (catalogs)
            Assert.assertTrue(op.isReportingCatalogs());
        else
            Assert.assertFalse(op.isReportingCatalogs());
        if (charsets)
            Assert.assertTrue(op.isReportingEncodings());
        else
            Assert.assertFalse(op.isReportingEncodings());
        if (entities)
            Assert.assertTrue(op.isReportingEntities());
        else
            Assert.assertFalse(op.isReportingEntities());
    }
}
