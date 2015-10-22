package com.kendallshaw.dtdnormalizer;

import java.io.InputStream;
import java.io.Reader;

public interface InputIdentifier {

    String getPublicId();
    void setPublicId(String publicId);

    String getSystemId();
    void setSystemId(String systemId);

    InputStream getByteStream();
    void setByteStream(InputStream byteStream);

    String getEncoding();
    void setEncoding(String encoding);

    Reader getReader();
    void setReader(Reader reader);
}
