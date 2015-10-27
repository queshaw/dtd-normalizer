package com.kendallshaw.xml;

public interface EntityResourceIdentifier {

    String getPublicId();
    void setPublicId(String publicId);

    String getSystemId();
    void setSystemId(String systemId);

    String getBaseUri();
    void setBaseUri(String uri);

    String getUri();
    void setUri(String uri);

    String getCharset();
    void setCharset(String charset);
}
