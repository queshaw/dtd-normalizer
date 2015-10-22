package com.kendallshaw.dtdnormalizer;

public enum Constants {
    ANY("any"),
    CONDITIONAL_SECTION("CONDITIONAL-SECTION"),
    CONDITION("CONDITION"),
    CONTENT_MODEL("content-model"),
    DATA("data"),
    DOCTYPE_DECLARATION("doctype-declaration"),
    DTDS("document-type-declarations"),
    DTD("document-type-declaration"),
    ELEMENT_DECLARATION("element-declaration"),
    ATTRIBUTE_LIST_DECLARATION("attribute-list-declaration"),
    REDEFINITION("redefinition"),
    STACK_TRACE("stack-trace"),
    MESSAGE("message"),
    FRAME("frame"),
    CLASS("class"),
    METHOD("method"),
    LINE_NUMBER("line-number"),
    C_IN("urn:os:stream:input"),
    C_OUT("urn:os:stream:output"),
    C_STDIN("urn:os:stream:stdin"),
    C_STDERR("urn:os:stream:stderr"),
    C_STDOUT("urn:os:stream:stdout"),
    ATTRIBUTE_DECLARATION("attribute-declaration"),
    STRING("string"),
    TOKEN("token"),
    NOTATION("notation"),
    ENUMERATION("enumeration"),
    ELEMENT("element"),
    EMPTY("empty"),
    ENCODING("encoding"),
    EXTERNAL_SUBSET("external-subset"),
    ENTITY_DECLARATION("entity-declaration"),
    TEXT("text"),
    RAW_TEXT("raw-text"),
    ENTITY("entity"),
    ERROR("error"),
    FATAL_ERROR("fatalError"),
    GROUP("group"),
    PARAMATER_ENTITY_DECLARATION("parameter-entity-declaration"),
    PCDATA("pcdata"),
    NAME("name"),
    OCCUR("occur"),
    OPTIONAL("?"),
    OR_DELIM("|"),
    PLUS("+"),
    PI("processing-instruction"),
    ROOT("root"),
    SEP("sep"),
    SEQ_DELIM(","),
    STAR("*"),
    TARGET("target"),
    TYPE("type"),
    VERSION("version"),
    WARNING("warning"),
    XML_DECLARATION("xml-declaration");

    private String rep;

    private Constants(String s) {
        rep = s;
    }

    public String toString() {
        return rep;
    }
}
