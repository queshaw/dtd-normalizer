package com.kendallshaw.dtdnormalizer.attributes;

public interface AttributeTypeWriter {

    void writeString(String s);

    void writeToken(AttributeToken t);

    void writeEnumeration(Iterable<String> it);

    void writeNotations(Iterable<String> it);
}
