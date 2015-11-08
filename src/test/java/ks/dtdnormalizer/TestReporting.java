package ks.dtdnormalizer;

import java.io.PrintStream;

import org.junit.Ignore;
import org.junit.Test;

import com.kendallshaw.dtdnormalizer.CommandLine;
import com.kendallshaw.dtdnormalizer.OptionParser;

public class TestReporting {

    @Ignore
    @Test
    public void reportEntitiesWithoutCatalog() {
        String[] args = new String[] {
                "--report", "entities",
                "src/test/resources/no-catalog/doc.xml"
        };
        OptionParser op = new OptionParser().parseCommandLineWithoutExit(args);
        CommandLine cl = new CommandLine();
        try {
            cl.report(op);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
