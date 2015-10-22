package com.kendallshaw.dtdnormalizer;

public interface XmlIdentifierResolver {

    InputIdentifier resolveInput(InputIdentifier id);

    OutputIdentifier resolveOutput(OutputIdentifier id);
}
