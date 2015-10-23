package com.kendallshaw.dtdnormalizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.xml.sax.InputSource;

public class CommandLine {

    public static void main(String[] args) {
        try {
            new CommandLine().normalize(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void normalize(String[] args) throws Exception {
        OptionParser op = null;
        try {
            op = new OptionParser().parseCommandLine(args);
            IdentifierResolver resolver =
                new IdentifierResolver(op.getCatalogList());
            InputSource input = resolver.resolveEntity(null, op.getInputPath());
            OutputIdentifier output =
                outputDestination(op.getOutputPath(), op.getCharset());
            XniConfiguration configuration = new XniConfiguration();
            op.parameterize(configuration);
            DtdNormalizer normalizer = new DtdNormalizer(configuration);
            Serialization serialization =
                serialization(normalizer, input, output, op);
            normalizer.setSerialization(serialization);
            normalizer.setIdentifierResolver(resolver);
            normalizer.setErrorHandler(new ErrorHandler(serialization));
            normalizer.setInput(input);
            normalizer.setOutput(output);
            normalizer.serialize();
        } catch (Exception e) {
            if (e.getMessage() == null) {
                e.printStackTrace();
            } else if (op == null || !op.isWithStacktrace())
                System.err.println(e.getMessage());
            else
                e.printStackTrace();
        }
    }

    public ErrorHandler errorHandler(Serialization s) {
        return new ErrorHandler(s);
    }

    public Serialization serialization(DtdNormalizer normalizer,
                                       InputSource in, OutputIdentifier out,
                                       OptionParser op)
        throws Exception
    {
        Serialization ser = null;
        if ("dtd".equals(op.getSerialization())) {
            DtdSerialization ds = new DtdSerialization(out.getByteStream(),
                                                       out.getEncoding());
            ds.setWithComments(op.isWithComments());
            ser = ds;
        } else {
            OutputStreamWriter osw = new OutputStreamWriter(out.getByteStream(),
                                                            out.getEncoding());
            ser = new XmlSerialization(osw);
        }
        return ser;
    }

    public InputSource inputSource(String path, Charset encoding)
        throws FileNotFoundException
    {
        String enc = encoding == null ? "UTF-8" : encoding.name();
        InputSource is = null;
        if ("-".equals(path)) {
            is = new InputSource(System.in);
            is.setEncoding(enc);
            is.setSystemId(Constants.C_STDIN.toString());
        } else {
            File f = new File(path);
            is = new InputSource(new FileInputStream(f));
            is.setSystemId(f.toURI().toASCIIString());
            is.setEncoding(enc);
        }
        return is;
    }

    public OutputIdentifier outputDestination(String path, Charset encoding)
        throws FileNotFoundException
    {
        String enc = encoding == null ? "UTF-8" : encoding.name();
        OutputIdentifier out = null;
        if ("-".equals(path)) {
            out = new OutputIdentifier(System.out, enc,
                                       Constants.C_STDOUT.toString());
        } else {
            File f = new File(path);
            FileOutputStream fos = new FileOutputStream(f);
            out = new OutputIdentifier(fos, enc, f.toURI().toASCIIString());
        }
        return out;
    }
}
