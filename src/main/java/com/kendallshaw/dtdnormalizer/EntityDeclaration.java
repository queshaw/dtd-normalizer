package com.kendallshaw.dtdnormalizer;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;

public class EntityDeclaration {

    private String name;

    private String text;

    private String rawText;

    private String publicId;

    private String systemId;

    private boolean internal = true;

    public EntityDeclaration(String name, XMLString text, XMLString rawText) {
        setName(name);
        setText(text.toString());
        setRawText(rawText.toString());
    }

    public EntityDeclaration(String name, XMLResourceIdentifier id) {
        setName(name);
        setPublicId(id.getPublicId());
        setSystemId(id.getLiteralSystemId());
        setInternal(false);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }
}
