package ks.dtdnormalizer;

import org.junit.Assert;
import org.junit.Test;

import com.kendallshaw.dtdnormalizer.CommandLine;

public class TestCommandLine {

    private static final String RESOURCE_DIR = System.getProperty("test.resource.dir");

    private static final String OUTPUT_DIR = System.getProperty("test.output.dir");

    @Test
    public void producesAHelpMessage() {
        try {
            CommandLine cl = new CommandLine(new String[] {
              OUTPUT_DIR,
              RESOURCE_DIR + "/dtd/catalog-dita.xml",
              RESOURCE_DIR + "/topic.xml"
            });
            cl.go();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
