package com.kendallshaw.dtdnormalizer.attributes;

public class AttributeDeclaration {

    public static final String CDATA = "CDATA";

    public static final String FIXED = "#FIXED";

    public static final String IMPLIED = "#IMPLIED";

    public static final String NOTATION = "NOTATION";

    public static final String REQUIRED = "#REQUIRED";

    private String nameText;

    private AttributeType attributeType;

    private AttributeToken tokenType;

    private AttributeEnumeration enumeration;

    private DefaultDeclaration defaultDeclaration;

    public AttributeDeclaration() { }

    public AttributeDeclaration(final String name,
                                final String type,
                                final String[] en,
                                final String defaultType,
                                final String defaultValue)
    {
        nameText = name;
        if (en == null) {
            if (CDATA.equals(type)) {
                attributeType = AttributeType.STRING;
            } else {
            	attributeType = AttributeType.TOKEN;
                tokenType = AttributeToken.valueOf(type);
            }
        } else {
            enumeration = new AttributeEnumeration(en);
            attributeType = AttributeType.ENUMERATION;
            enumeration.setNotation(NOTATION.equals(type));
        }
        defaultDeclaration = new DefaultDeclaration(defaultType, defaultValue);
    }

    public String name() {
        return nameText;
    }

    public AttributeType attributeType() {
        return attributeType;
    }

    public AttributeToken tokenType() {
        return tokenType;
    }

    public AttributeEnumeration enumeration() {
        return enumeration;
    }

    public DefaultDeclaration defaultDeclaration() {
        return defaultDeclaration;
    }
}
