package com.kendallshaw.dtdnormalizer;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.xerces.parsers.XML11Configuration;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.xml.sax.InputSource;

public class XniConfiguration extends XML11Configuration {

    private Set<String> inclusionEntityNames = new HashSet<String>();

    private Map<String, String> inclusionIds;

    private Map<String, Set<String>> additionalEntityNames;

    private boolean includingAll = false;

    public XniConfiguration() { }

    public XniConfiguration(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public XniConfiguration(SymbolTable symbolTable,
                            XMLGrammarPool grammarPool)
    {
        super(symbolTable, grammarPool);
    }

    public XniConfiguration(SymbolTable symbolTable,
                            XMLGrammarPool grammarPool,
                            XMLComponentManager parentSettings)
    {
        super(symbolTable, grammarPool, parentSettings);
    }

    public void initialize() throws Exception {
        setFeature(VALIDATION, true);
        setFeature(ALLOW_JAVA_ENCODINGS, true);
        setFeature(EXTERNAL_GENERAL_ENTITIES, true);
        setFeature(EXTERNAL_PARAMETER_ENTITIES, true);
        final String dups =
            System.getProperty("containerguesser.duplicates.warnings");
        final boolean warn = "true".equals(dups) || "yes".equals(dups);
        setFeature(WARN_ON_DUPLICATE_ATTDEF, warn);
        setFeature(WARN_ON_DUPLICATE_ENTITYDEF, warn);
    }


    public Map<String, Set<String>> getAdditionalEntityNames() {
        return additionalEntityNames;
    }

    public void setAdditionalEntityNames(Map<String, Set<String>>
                                         additionalEntityNames)
    {
        this.additionalEntityNames = additionalEntityNames;
    }

    public Map<String, String> getInclusionIds() {
        if (inclusionIds == null)
            inclusionIds = new Hashtable<String, String>();
        return inclusionIds;
    }

    public void setInclusionEntities(Map<String, String> inclusionIds) {
        this.inclusionIds = inclusionIds;
    }

    public Set<String> getInclusionEntityNames() {
        return inclusionEntityNames;
    }

    public void setInclusionEntityNames(Set<String> inclusionEntityNames) {
        this.inclusionEntityNames = inclusionEntityNames;
    }

    public boolean isIncludingAll() {
        return includingAll;
    }

    public void setIncludingAll(boolean f) {
        includingAll = f;
    }

    public void parse(InputSource is) throws XNIException, IOException {
        parse(xniInputSource(is));
    }

    public XMLInputSource xniInputSource(InputSource is) {
        Reader r = is.getCharacterStream();
        if (r == null) {
            return new XMLInputSource(is.getPublicId(),
                    is.getSystemId(),
                    is.getSystemId(),
                    is.getByteStream(),
                    is.getEncoding());
        } else {
            return new XMLInputSource(is.getPublicId(),
                    is.getSystemId(),
                    is.getSystemId(),
                    r,
                    is.getEncoding());
        }
    }
}
