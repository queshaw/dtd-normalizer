package com.kendallshaw.dtdnormalizer;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class OutputIdentifier {

    private OutputStream byteStream = null;

    private Writer writer = null;

    private String encoding = null;

    private String systemId = null;

    public OutputIdentifier() { }

    public OutputIdentifier(String systemId) {
        setSystemId(systemId);
    }

    public OutputIdentifier(Writer writer, String systemId) {
        setWriter(writer);
        setSystemId(systemId);
    }

    public OutputIdentifier(OutputStream byteStream, String encoding,
                            String systemId)
    {
        setByteStream(byteStream);
        setSystemId(systemId);
    }

    public OutputStream getByteStream() {
        return byteStream;
    }

    public void setByteStream(OutputStream byteStream) {
        this.byteStream = byteStream;
    }

    public Writer getWriter() {
        return writer;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getEncoding() {
        if (encoding == null)
            encoding = "UTF-8";
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
