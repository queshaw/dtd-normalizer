package com.kendallshaw.dtdnormalizer.attributes;

public class DefaultDeclaration {

    private String fixed;

    private String value;

    private DefaultType defaultType;

    public DefaultDeclaration() {
        this(null, "");
    }

    public DefaultDeclaration(final String type,
                              final String value)
    {
        if (value == null)
            defaultType = DefaultType.fromString(type);
        else {
            final String f = AttributeDeclaration.FIXED;
            if (f.equals(type) || type == null) {
                fixed = f;
                this.value = normalizedValue(value, '\"');
            }
        }
    }

    public DefaultType type() {
        return defaultType;
    }

    public String fixed() {
        return fixed; 
    }

    public String value() {
        return value; 
    }

    public static String normalizedValue(final String text, final char delim) {
        char other = '"' == delim ? '\'' : '"';
        return text.replace(delim, other);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((defaultType == null) ? 0 : defaultType.hashCode());
        result = prime * result + ((fixed == null) ? 0 : fixed.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultDeclaration other = (DefaultDeclaration) obj;
        if (defaultType != other.defaultType)
            return false;
        if (fixed == null) {
            if (other.fixed != null)
                return false;
        } else if (!fixed.equals(other.fixed))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}

