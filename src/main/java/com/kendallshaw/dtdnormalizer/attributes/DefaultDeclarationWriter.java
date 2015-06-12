package com.kendallshaw.dtdnormalizer.attributes;

public interface DefaultDeclarationWriter {

    void writeRequired();

    void writeImplied();

    void writeValue(String v);
}
