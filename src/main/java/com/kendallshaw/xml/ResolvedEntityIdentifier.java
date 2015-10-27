package com.kendallshaw.xml;

public class ResolvedEntityIdentifier implements EntityResourceIdentifier {

    private String publicId;

    private String systemId;

    private String baseUri;

    private String uri;

    private String charset;

    public ResolvedEntityIdentifier() { }

    public ResolvedEntityIdentifier(String uri, String charset)
    {
        this(null, uri, charset);
    }

    public ResolvedEntityIdentifier(String publicId, String uri, String charset)
    {
        this(publicId, null, null, uri, charset);
    }

    public ResolvedEntityIdentifier(String publicId, String systemId,
                                    String baseUri,
                                    String uri,
                                    String charset)
    {
        setPublicId(publicId);
        setSystemId(systemId);
        setBaseUri(baseUri);
        setUri(uri);
        setCharset(charset);
    }

    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String uri) {
        this.baseUri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    @Override
    public void setCharset(String charset) {
        this.charset = charset;
    }
}
