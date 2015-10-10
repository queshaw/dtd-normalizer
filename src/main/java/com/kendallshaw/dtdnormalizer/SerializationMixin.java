package com.kendallshaw.dtdnormalizer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.xni.XNIException;

public class SerializationMixin {

    protected final static Pattern EREX = Pattern.compile("(%|&#)([^\\s;]+);");

    protected final static String ATTLIST = "ATTLIST";

    protected final static String CDATA = "CDATA";

    protected final static String DOCTYPE = "DOCTYPE";

    protected final static String ELEMENT = "ELEMENT";

    protected final static String ENTITIES = "ENTITIES";

    protected final static String ENTITY = "ENTITY";

    protected final static String FIXED = "#FIXED";

    protected final static String ID = "ID";

    protected final static String IDREF = "IDREF";

    protected final static String IDREFS = "IDREFS";

    protected final static String IMPLIED = "#IMPLIED";

    protected final static String NDATA = "NDATA";

    protected final static String NMTOKEN = "NMTOKEN";

    protected final static String NMTOKENS = "NMTOKENS";

    protected final static String NOTATION = "NOTATION";

    protected final static String PUBLIC = "PUBLIC";

    protected final static String REQUIRED = "#REQUIRED";

    protected final static String SYSTEM = "SYSTEM";

    public void badstackTrace(final Exception e) throws XNIException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        ps.close();
        //out(baos.toString());
    }

    public String normalizedText(final String text)
        throws XNIException
    {
        return text.replaceAll("[ \t\n]{2,}", " ").trim();
    }

    public List<String> entityText(final String text) {
        final List<String> strings = new ArrayList<String>();
        final Matcher m = EREX.matcher(text);
        int end = 0;
        int prev = 0;
        while (m.find(end)) {
            int start = m.start();
            end = m.end();
            strings.add(normalizedText(text.substring(prev, start)));
            strings.add(m.group(1));
            strings.add(m.group(2));
            strings.add(";");
            prev = end;
        }
        if (end > 0)
            strings.add(normalizedText(text.substring(end)));
        return strings;
    }
}
