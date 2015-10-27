package com.kendallshaw.dtdnormalizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ibm.icu.charset.CharsetICU;
import com.kendallshaw.net.ResourceUtils;

public class OptionParser {

    @SuppressWarnings("serial")
    public static class OptionParseError extends RuntimeException {
        public OptionParseError() { super(); }
        public OptionParseError(String msg) { super(msg); }
        public OptionParseError(Throwable t) { super(t); }
    }

    public static final String COMMENTS_PROPERTY = "dtd-normalizer.comments";

    public static final String ENTITIES_PROPERTY = "dtd-normalizer.entities";

    public static final String SERIALIZATION_PROPERTY =
        "dtd-normalizer.serialization";

    public static final String CHARSET_PROPERTY =
        "dtd-normalizer.charset";

    private static final String CMD_LINE = CommandLine.class.getName()
        + "\n       [options] [--] input-file [output-file]";

    private static final String HELP_HEADER =
        "Creates a simplified representation of the input document's DTD.\n\n"
        + "Options:\n\n";

    private static final String HELP_FOOTER =
        "\nPositional arguments may be specified before, after or between"
        + " options. A double-hyphen with no option name (--) can be used to"
        + " indicate that all following arguments are positional."
        + " Options take precendence over system properties. If no output file"
        + " is specified, the output is written to standard out."
        + "\n\nReport errors to http://github.com/queshaw/dtd-normalizer.";

    private static final String CATALOGS_DESC =
        String.format("(-D%s) Specifies a list of OASIS XML catalog files,"
                      + " separated by semi-colons, which can be used in"
                      + " resolving entity resources.",
                      "xml.catalog.files");

    private static final String SERIALIZATION_DESC =
        String.format("(-D%s) xml/dtd. default: xml. Specifies XML or DTD"
                      + " syntax output.", SERIALIZATION_PROPERTY);

    private static final String CHARSET_DESC =
        String.format("(-D%s) default: UTF-8. Specifies the charset for"
                      + " encoding output.", CHARSET_PROPERTY);

    private static final String LIST_CHARSETS_DESC =
        String.format("Lists the charsets supported for encoding output.");

    private static final String REPORT_DESC =
        String.format("encodings/entities/catalogs/all default: all."
                      + " Lists external entities, their character encoding,"
                      + " or catalogs loaded, multiple values can be specified,"
                      + " separated by commas.");

    private static final String COMMENTS_DESC =
        String.format("(-D%s) true/yes/false/no default: false. Specifies"
                      + " that parameter entity locations are added as"
                      + " comments to the DTD serialization.",
                      COMMENTS_PROPERTY);

    private static final String ENTITIES_DESC =
        String.format("(-D%s) Specifies a file containing a list of public"
                      + " identifiers of entity sets to be included in the"
                      + " DTD  serialization.", ENTITIES_PROPERTY);

    private static final String STACKTRACE_DESC =
        String.format("Includes a stacktrace, if possible, after an error"
                      + " occurs.");

    private static final String HELP_DESC =
        "Writes this message to standard out.";

    private static final String CATALOGS_OPT = "C";

    private static final String SERIALIZATION_OPT = "s";

    private static final String CHARSET_LONGOPT = "charset";

    private static final String LIST_CHARSETS_LONGOPT = "charsets";

    private static final String REPORT_LONGOPT = "report";

    private static final String COMMENTS_OPT = "c";

    private static final String ENTITIES_OPT = "e";

    private static final String HELP_OPT = "h";

    private static final String STACKTRACE_LONGOPT = "stacktrace";

    private String inputPath = null;

    private String outputPath = null;

    private String catalogList = null;

    private String serialization = null;

    private Charset charset = null;

    private boolean reporting = false;

    private boolean reportingEncodings = false;

    private boolean reportingEntities = false;

    private boolean reportingCatalogs = false;

    private boolean withComments = false;

    private String entitiesPath = null;

    private Map<String, String> inclusionIds =
        new Hashtable<String, String>();

    private boolean includingAll = false;

    private boolean withStacktrace = false;

    private Pattern EREX =
        Pattern.compile("^(\\s*|#.*|(public|system):\\s*(.*))$");

    private Pattern OREX =
        Pattern.compile("^--?[^= \t]+");

    private URI currentDirectory; 

    public OptionParser() {
        try {
            currentDirectory = new File(".").getCanonicalFile().toURI();
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);;
        }
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getCatalogList() {
        return catalogList;
    }

    public void setCatalogList(String catalogList) {
        this.catalogList = catalogList;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public boolean isReporting() {
        return reporting;
    }

    public void setReporting(boolean reporting) {
        this.reporting = reporting;
    }

    public boolean isReportingEncodings() {
        return reportingEncodings;
    }

    public void setReportingEncodings(boolean reportingEncodings) {
        this.reportingEncodings = reportingEncodings;
    }

    public boolean isReportingEntities() {
        return reportingEntities;
    }

    public void setReportingEntities(boolean reportingEntities) {
        this.reportingEntities = reportingEntities;
    }

    public boolean isReportingCatalogs() {
        return reportingCatalogs;
    }

    public void setReportingCatalogs(boolean reportingCatalogs) {
        this.reportingCatalogs = reportingCatalogs;
    }

    public boolean isWithComments() {
        return withComments;
    }

    public void setWithComments(boolean withComments) {
        this.withComments = withComments;
    }

    public String getEntitiesPath() {
        return entitiesPath;
    }

    public void setEntitiesPath(String entitiesPath) {
        this.entitiesPath = entitiesPath;
    }

    public void setIncludingAll(boolean f) {
        includingAll = f;
    }

    public boolean isIncludingAll() {
        return includingAll;
    }

    public Map<String, String> inclusionIds() {
        return inclusionIds;
    }

    public boolean isWithStacktrace() {
        return withStacktrace;
    }

    public void setWithStacktrace(boolean withStacktrace) {
        this.withStacktrace = withStacktrace;
    }

    public OptionParser parseCommandLine(String[] args) {
        try {
            OptionParser op = parseCommandLineWithoutExit(args);
            if (op == null)
                System.exit(1);
            return op;
        } catch (OptionParseError e) {
            System.exit(1);
            return null; // ...
        }
    }

    public OptionParser parseCommandLineWithoutExit(String[] args) {
        Options opts = createOptions(args);
        try {
            org.apache.commons.cli.CommandLine cl =
                new DefaultParser().parse(opts, args, false);

            if (cl.hasOption(HELP_OPT)) {
                usage(opts, System.out);
                return null;
            }

            if (cl.hasOption(LIST_CHARSETS_LONGOPT)) {
                listCharsets();
                return null;
            }

            parsePositionalArgs(opts, cl.getArgs());

            Properties properties = cl.getOptionProperties("D");
            String catalogList = parseOption(properties,
                                             cl.getOptionValue(CATALOGS_OPT),
                                             "xml.catalog.files");
            setCatalogList(parseCatalogList(catalogList));

            parseEntities(properties, opts,
                          cl.getOptionValue(ENTITIES_OPT),
                          cl.hasOption(ENTITIES_OPT),
                          ENTITIES_PROPERTY);

            if (parseReport(opts,
                            cl.getOptionValue(REPORT_LONGOPT),
                            cl.hasOption(REPORT_LONGOPT)))
            {
                setReporting(true);
                return this;
            }

            parseComments(parseOptionalOption(properties,
                                              cl.getOptionValue(COMMENTS_OPT),
                                              cl.hasOption(COMMENTS_OPT),
                                              COMMENTS_PROPERTY),
                          opts);
            
            parseSerialization(parseOption(properties,
                                           cl.getOptionValue(SERIALIZATION_OPT),
                                           SERIALIZATION_PROPERTY),
                               opts);

            parseCharset(parseOption(properties,
                                     cl.getOptionValue(CHARSET_LONGOPT),
                                     CHARSET_PROPERTY),
                         CHARSET_LONGOPT,
                         opts);

            setWithStacktrace(cl.hasOption(STACKTRACE_LONGOPT));
        } catch (OptionParseError e) {
            System.err.println(e.getMessage());
            usage(opts);
            throw e;
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            usage(opts);
            throw new OptionParseError(e);
        }
        return this;
    }

    public void usage(Options opts) {
        usage(opts, System.err);
    }

    public void usage(Options opts, OutputStream os) {
        HelpFormatter hf = new HelpFormatter();
        PrintWriter pw = new PrintWriter(os);
        hf.setSyntaxPrefix("Usage: java ");
        hf.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                if ("D".equals(o2.getOpt()))
                    return 1;
                if ("h".equals(o2.getOpt()))
                    return -1;
                return o1.getOpt().compareTo(o2.getOpt());
            }
        });
        int lpad = HelpFormatter.DEFAULT_LEFT_PAD;
        int dpad = HelpFormatter.DEFAULT_DESC_PAD;
        hf.printHelp(pw,
                     80,
                     CMD_LINE,
                     HELP_HEADER,
                     opts,
                     lpad,
                     dpad,
                     HELP_FOOTER,
                     false);
        pw.flush();
    }

    public void parameterize(XniConfiguration configuration) {
        configuration.setInclusionEntities(inclusionIds());
        configuration.setIncludingAll(isIncludingAll());
    }

    private String canonicalFilePath(String path)
        throws IllegalArgumentException
    {
        try {
            return ResourceUtils.canonicalFilePath(path); 
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String checkInputPath(String path, Options opts) {
        String msg = null;
        try {
            if (path == null)
                return null;
            if ("-".equals(path))
                return "-";
            File f = new File(path);
            if (!f.exists()) {
                msg = path + " could not be found.";
            } else if (!f.isFile()) {
                msg = path + " can not be read.";
            } else {
                return f.getCanonicalPath();
            }
        } catch (IOException e) {
            msg = e.getMessage();
        }
        System.err.println(msg);
        usage(opts);
        throw new OptionParseError(msg);
    }

    private String checkOutputPath(String path, Options opts) {
        String msg = null;
        if (path == null)
            return null;
        if ("-".equals(path))
            return "-";
        File f = new File(path);
        try {
            File absFile = f.getCanonicalFile();
            File parent = f.getCanonicalFile().getParentFile();
            if (!parent.isDirectory()) {
                msg = parent + " is not a directory.";
            } else {
                return absFile.getAbsolutePath();
            }
        } catch (IOException e) {
            msg = e.getMessage();
        }
        System.err.println(msg);
        usage(opts);
        throw new OptionParseError(msg);
    }

    private void parseComments(String s, Options opts) {
        Boolean f = null;
        try {
            f = "".equals(s) ? true : parseBoolean(s);
        } catch (OptionParseError e) {
            String msg = "The parameter value " + s
                       + " was not recognized.";
            throw new OptionParseError(msg);
        }
        setWithComments(f);
    }

    private String parseCatalogList(String catalogList) {
        if (catalogList == null)
            return null;
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        StringTokenizer st = new StringTokenizer(catalogList, ";");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (first)
                first = false;
            else
                sb.append(";");
            sb.append(canonicalFilePath(s));
        }
        return sb.toString();
    }

    private boolean parseReport(Options opts, String optionValue,
                                boolean specified)
    {
        if (!specified)
            return false;

        Set<String> values = new HashSet<String>();

        StringTokenizer st = new StringTokenizer(optionValue, ",");
        boolean first = true;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (first)
                first = false;
            else
                values.add(s);
        }

        boolean all = false;
        if (optionValue == null || values.contains("all"))
            all = true;
        if (values.contains("catalogs") || all)
            setReportingCatalogs(true);
        if (values.contains("charsets") || all)
            setReportingEncodings(true);
        if (values.contains("entities") || all)
            setReportingEntities(true);
        return specified;
    }

    private void parseEntities(Properties properties, Options opts,
                               String optionValue, boolean hasOption,
                               String propertyName)
    {
        String value = parseOptionalOption(properties, optionValue,
                                           hasOption, propertyName);
        if (value == null)
            setEntitiesPath(null);
        else if ("".equals(value)) {
            setEntitiesPath(null);
            setIncludingAll(true);
        } else {
            String path = checkInputPath(value, opts);
            setEntitiesPath(path);
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            try {
                try {
                    fis = new FileInputStream(new File(path));
                    isr = new InputStreamReader(fis);
                    br = new BufferedReader(isr);
                    String line = br.readLine();
                    int lineNo = 0;
                    while (line != null) {
                        ++lineNo;
                        Matcher m = EREX.matcher(line);
                        line = br.readLine();
                        if (!m.matches()) {
                            String msg = "Unexpected text on line " + lineNo
                                + " in entity set list " + path + ".";
                            throw new OptionParseError(msg);
                        } else if ("".equals(m.group(1).trim())) {
                        } else if (m.group(1).charAt(0)=='#') {
                        } else if ("public".equals(m.group(2))) {
                            inclusionIds.put(m.group(3), "public");
                        } else if ("system".equals(m.group(2))) {
                            inclusionIds.put(m.group(3), "system");
                        } else {
                            String msg = "Internal error parsing line "
                                       + lineNo + " of entity set list "
                                       + path + ".";
                            System.err.println(msg);
                        }
                    }
                } finally {
                    if (br != null)
                        br.close();
                    if (isr != null)
                        isr.close();
                    if (fis != null)
                        fis.close();
                }
                if (inclusionIds.isEmpty()) {
                    String msg = "No identifiers found in " + path + ".";
                    throw new OptionParseError(msg);
                }
            } catch (Exception e) {
                if (e instanceof OptionParseError)
                    throw (OptionParseError) e;
                throw new OptionParseError(e.getMessage());
            }
        }
    }

    private void parseSerialization(String s, Options opts) {
        if (s == null || "xml".equals(s) || "dtd".equals(s))
            setSerialization(s);
        else {
            String msg = "The parameter value " + s
                       + " was not recognized.";
            throw new OptionParseError(msg);
        }
    }

    private boolean parseCharset(String s, String opt, Options opts) {
        Charset ch = null;
        try {
            if (s != null) {
                ch = CharsetICU.forName(s);
            }
        } catch (IllegalCharsetNameException e) {
            throw new  OptionParseError(e);
        } catch (UnsupportedCharsetException e) {
            throw new  OptionParseError(e);
        }
        if (CHARSET_LONGOPT.equals(opt)) {
            setCharset(ch);
        }
        return ch != null;
    }

    private Boolean parseBoolean(String s) {
        if (s == null)
            return false;
        if ("yes".equalsIgnoreCase(s))
            return true;
        if ("true".equalsIgnoreCase(s))
            return true;
        if ("no".equalsIgnoreCase(s))
            return false;
        if ("false".equalsIgnoreCase(s))
            return false;
        throw new OptionParseError("Invalid boolean option: " + s);
    }

    private String parseOption(Properties props, String optionValue,
                               String propertyName)
    {
        String value = optionValue;
        if (value == null) {
            String prop = System.getProperty(propertyName);
            value = props.getProperty(propertyName, prop);
        }
        return value;
    }

    private String parseOptionalOption(Properties props, String optionValue,
                                       boolean specified, String propertyName)
    {
        String value = optionValue;
        if (value == null) {
            if (specified)
                return "";
            String prop = System.getProperty(propertyName);
            value = props.getProperty(propertyName, prop);
        }
        return value;
    }

    private void parsePositionalArgs(Options opts, String[] posArgs) {
        if (posArgs.length < 1) {
            throw new OptionParseError("No input was specified.");
        } else {
            for (String a : posArgs) {
                Matcher m = OREX.matcher(a);
                if (m.find()) {
                    String msg = "Unrecognized option " + a;
                    throw new OptionParseError(msg);
                }
            }
            if (posArgs.length > 2) {
                String msg = "Too many positional arguments";
                System.err.println(msg + ":");
                for (String a : posArgs)
                    System.err.printf("%" + HelpFormatter.DEFAULT_LEFT_PAD
                                      + "s%s\n", " ", a);
                throw new OptionParseError(msg + ".");
            }
        }
        setInputPath(checkInputPath(posArgs[0], opts));
        if (posArgs.length == 2) {
            setOutputPath(checkOutputPath(posArgs[1], opts));
        } else {
            setOutputPath("-");
        }
    }

    private Options createOptions(String[] args) {
        Options opts = new Options();
        Option catalog =
            Option.builder(CATALOGS_OPT)
            .desc(CATALOGS_DESC)
            .argName("path[...;path]")
            .numberOfArgs(1)
            .longOpt("catalogs")
            .build();
        opts.addOption(catalog);
        Option serialization =
            Option.builder(SERIALIZATION_OPT)
            .desc(SERIALIZATION_DESC)
            .argName("format")
            .numberOfArgs(1)
            .longOpt("serialization")
            .build();
        opts.addOption(serialization);
        Option comments =
            Option.builder(COMMENTS_OPT)
            .desc(COMMENTS_DESC)
            .argName("boolean")
            .numberOfArgs(1)
            .optionalArg(true)
            .longOpt("comments")
            .build();
        opts.addOption(comments);
        Option charset =
            Option.builder(CHARSET_LONGOPT)
            .desc(CHARSET_DESC)
            .argName("charset")
            .longOpt(CHARSET_LONGOPT)
            .numberOfArgs(1)
            .build();
        opts.addOption(charset);
        Option listCharsets =
            Option.builder(LIST_CHARSETS_LONGOPT)
            .desc(LIST_CHARSETS_DESC)
            .longOpt(LIST_CHARSETS_LONGOPT)
            .build();
        opts.addOption(listCharsets);
        Option report =
            Option.builder(REPORT_LONGOPT)
            .desc(REPORT_DESC)
            .longOpt(REPORT_LONGOPT)
            .argName("type[...,type]")
            .numberOfArgs(1)
            .optionalArg(true)
            .build();
        opts.addOption(report);
        Option stackTrace =
            Option.builder(STACKTRACE_LONGOPT)
            .desc(STACKTRACE_DESC)
            .longOpt(STACKTRACE_LONGOPT)
            .build();
        opts.addOption(stackTrace);
        Option entities =
            Option.builder(ENTITIES_OPT)
            .desc(ENTITIES_DESC)
            .argName("path")
            .numberOfArgs(1)
            .optionalArg(true)
            .longOpt("entities")
            .build();
        opts.addOption(entities);
        Option props =
            Option.builder("D")
            .desc("System properties.")
            .argName("property=value")
            .numberOfArgs(2)
            .valueSeparator()
            .build();
        opts.addOption(props);
        Option help =
            Option.builder(HELP_OPT)
            .desc(HELP_DESC)
            .longOpt("help")
            .build();
        opts.addOption(help);
        return opts;
    }

    private void listCharsets() {
        Map<String, Charset> charsets = CharsetICU.availableCharsets();
        String[] keys = charsets.keySet().toArray(new String[0]);
        Arrays.sort(keys);
            for (String key : keys) {
                Charset ch = charsets.get(key);
                System.out.println("\nName: " + ch.name());
                System.out.println("Display Name: " + ch.displayName());
                System.out.println("IANA Registered: " + ch.isRegistered());
                Set<String> aliases = ch.aliases();
                if (!aliases.isEmpty())
                    System.out.println("Aliases:");
                for (String a : aliases)
                    System.out.println("  " + a);
            }
    }
}
