package ks.dtdnormalizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
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
                "--entities", "--",
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
}
