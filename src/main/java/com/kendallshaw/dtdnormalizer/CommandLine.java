package com.kendallshaw.dtdnormalizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.xni.parser.XMLInputSource;

public final class CommandLine {

    public static final String COMMENTS = "dtd-normalizer.comments";

    public static final String SERIALIZATION = "dtd-normalizer.serialization";

    private String catalogPath;

    private String pathSpec;

    private List<String> inputPaths = new ArrayList<String>();

    private List<String> outputPaths = new ArrayList<String>();

    public CommandLine(final String[] args) {
        if (args.length < 3) {
            final String name = getClass().getName();
            System.err.printf("Usage: %s ( output-directory | output-file ) catalog-path ( xml-path | @file-list-file )\n", name);
            System.err.println("\nSystem properties:\n");
            System.err.println("dtd-normalizer.comments=(true|false) (default: false)   Adds entity location comments.");
            System.err.println("dtd-normalizer.serialization=(xml|dtd) (default: xml)   Selects XML or DTD text output.");
            System.exit(1);
        }
        outputPaths.add(args[0]);
        catalogPath = args[1];
        pathSpec = args[2];
    }

    public static void main(final String[] args) throws Exception {
        final CommandLine c = new CommandLine(args);
        c.go();
    }

    public void go() throws Exception {
        parsePaths(pathSpec);

        File f = new File(new URI(outputPaths.get(0)));
        Serialization out = null;
        if ("xml".equals(System.getProperty(SERIALIZATION, "xml"))) {
            XmlSerialization s = new XmlSerialization(f);
            out = s;
        } else {
            DtdSerialization s =
                new DtdSerialization(new File(new URI(outputPaths.get(0))));
            s.setWithComments(Boolean.parseBoolean(System.getProperty(COMMENTS, "false")));
            out = s;
        }
        final XniConfiguration configuration = new XniConfiguration();
        final DtdHandler tracer = new DtdHandler(out, configuration);
        final DocumentHandler dh = new DocumentHandler(out, configuration);
        configuration.setDocumentHandler(dh);
        configuration.setEntityResolver(new IdentifierResolver(catalogPath));
        configuration.setErrorHandler(new ErrorHandler(out));
        configuration.setDTDContentModelHandler(tracer);
        configuration.setDTDHandler(tracer);
        configuration.initialize();
        parseDocuments(configuration);
    }

    private void parsePaths(final String paths) throws Exception {
        if (!paths.startsWith("@")) {
            inputPaths.add(paths);
        } else {
            final File ff = new File(paths.substring(1));
            final File dir = ff.getAbsoluteFile().getParentFile();
            final FileInputStream fis = new FileInputStream(ff);
            final InputStreamReader isr = new InputStreamReader(fis);
            final BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            while (line != null) {
                inputPaths.add(new File(dir, line).getAbsolutePath());
                line = br.readLine();
            }
            br.close();
        }
        parseOutputFile();
    }

    private void parseOutputFile() throws Exception {
        File out = new File(outputPaths.get(0));
        outputPaths.clear();
        if (inputPaths.size() > 1 || out.isDirectory()) {
            out.mkdirs();
            URI dir = new File(out, "ignored").toURI();
            for (final String f : inputPaths) {
                File in = new File(f);
                URI resolved = dir.resolve(in.getName());
                outputPaths.add(resolved.toString());
            }
        } else {
            outputPaths.add(out.toURI().toString());
        }
    }

    private void parseDocuments(final XniConfiguration cfg) throws Exception {
        Serialization s =
            ((DocumentHandler)cfg.getDocumentHandler()).getSerializer();
        for (int i = 0, e = inputPaths.size(); i < e; ++i) {
            s.resetTargetResource(new URI(outputPaths.get(i)));
            final XMLInputSource xis =
                new XMLInputSource(null, inputPaths.get(i), null);
            cfg.parse(xis);
        }
    }
}
