package com.kendallshaw.dtdnormalizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.xerces.xni.parser.XMLInputSource;

public final class CommandLine {

    public static final String COMMENTS = "dtd-normalizer.comments";

    public static final String ENTITIES = "dtd-normalizer.entities";

    public static final String SERIALIZATION = "dtd-normalizer.serialization";

    private String catalogPath;

    private String pathSpec;

    private List<String> inputPaths = new ArrayList<String>();

    private List<String> outputPaths = new ArrayList<String>();

    public CommandLine(final String[] args) {
        if (args.length < 3) {
            final String name = getClass().getName();
            System.err.printf("Usage: %s output-file catalog-path xml-path\n", name);
            System.err.println("");
            System.err.println("System properties:");
            System.err.println("");
            System.err.println("dtd-normalizer.serialization=(xml|dtd) (default: xml)");
            System.err.println("");
            System.err.println("    Selects XML or DTD text output.");
            System.err.println("");
            System.err.println("dtd-normalizer.comments=(true|false) (default: false)");
            System.err.println("");
            System.err.println("    Adds entity location comments to the DTD serialization.");
            System.err.println("");
            System.err.println("dtd-normalizer.entities=path");
            System.err.println("");
            System.err.println("    Specifies a file containing a list of public identifiers");
            System.err.println("    of entity sets to be included in the DTD serialization.");
            System.err.println("");
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
        parsePublicIds(tracer.inclusionPublicIds());
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

    private void parsePublicIds(Set<String> inclusionEntities) {
        String publicIdFile = System.getProperty(ENTITIES);
        if (publicIdFile == null)
            return;
        File f = new File(publicIdFile);
        if (!f.exists())
            System.err.println(publicIdFile + " does not exist.");
        if (f.isDirectory())
            System.err.println(publicIdFile + " must not be a directory.");
        else if (!f.isFile())
            System.err.println(publicIdFile + " can not be read.");
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            try {
                fis = new FileInputStream(publicIdFile);
                isr = new InputStreamReader(fis, "UTF-8");
                br = new BufferedReader(isr);
                String line = br.readLine();
                while (line != null) {
                    inclusionEntities.add(line);
                    line = br.readLine();
                }
            } finally {
                if (br != null)
                    br.close();
                else if (isr != null)
                    isr.close();
                else if (fis != null)
                    fis.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
