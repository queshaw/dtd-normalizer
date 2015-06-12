package com.kendallshaw.dtdnormalizer.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AttributeEnumeration
    implements Iterator<String>, Iterable<String>
{
    private boolean notation = false;

    private List<String> enumeration = null;

    private Iterator<String> iterator = null;

    public AttributeEnumeration() {
        this(null);
    }

    public AttributeEnumeration(final String[] values) {
        if (values == null)
            enumeration = new ArrayList<String>();
        else
            enumeration = new ArrayList<String>(Arrays.asList(values));
    }

    public boolean isNotation() {
        return notation;
    }

    public void setNotation(boolean f) {
        notation = f;
    }

    @Override
    public boolean hasNext() {
        return iterator().hasNext();
    }

    @Override
    public String next() {
        return iterator().next();
    }

    @Override
    public void remove() {
        iterator().remove();
    }

    public Iterator<String> iterator() {
        if (iterator == null)
            iterator = enumeration.iterator();
        return iterator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((enumeration == null) ? 0 : enumeration.hashCode());
        result = prime * result + (notation ? 1231 : 1237);
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
        AttributeEnumeration other = (AttributeEnumeration) obj;
        if (enumeration == null) {
            if (other.enumeration != null)
                return false;
        } else if (!enumeration.equals(other.enumeration))
            return false;
        if (notation != other.notation)
            return false;
        return true;
    }
}
