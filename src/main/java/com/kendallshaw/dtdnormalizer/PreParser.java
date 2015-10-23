package com.kendallshaw.dtdnormalizer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dtd.XMLDTDLoader;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

public class PreParser extends XMLDTDLoader implements Patterns {

    public static final String GRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;

    public static final String NAMESPACES_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;

    public static final String VALIDATION_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;

    public static final int BIG_PRIME = 2039;

    private XMLParserConfiguration configuration;

    private int entityDepth = 0;

    private Stack<String> entityStack = new Stack<String>();

    private Map<String, String> inclusionIds;

    private Set<String> entityDeclNames = new HashSet<String>();

    private Set<String> inclusionEntityNames = new HashSet<String>();

    protected PreParser() { }

    public PreParser(XMLParserConfiguration configuration) {
        setConfiguration(configuration);
        if (XniConfiguration.class.isAssignableFrom(configuration.getClass())) {
            XniConfiguration cfg = (XniConfiguration) configuration;
            setInclusionIds(cfg.getInclusionIds());
            Set<String> names =
                cfg.getInclusionEntityNames();
            setInclusionEntityNames(names);
        }
    }

    public Map<String, String> getInclusionIds() {
        return inclusionIds;
    }

    public void setInclusionIds(Map<String, String> inclusionIds) {
        this.inclusionIds = inclusionIds;
    }

    public Set<String> getInclusionEntityNames() {
        return inclusionEntityNames;
    }

    public void setInclusionEntityNames(Set<String> inclusionEntityNames) {
        this.inclusionEntityNames = inclusionEntityNames;
    }

    public XMLParserConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(XMLParserConfiguration configuration) {
        this.configuration = configuration;
    }

    public void parse(String publicId, String systemId, String baseSystemId)
        throws IOException
    {
        XMLResourceIdentifierImpl rid =
            new XMLResourceIdentifierImpl(publicId, systemId, baseSystemId,
                                          systemId);
        XMLEntityResolver res = getConfiguration().getEntityResolver();
        XMLInputSource is = null;
        if (res == null)
            is = new XMLInputSource(rid);
        else
            is = res.resolveEntity(rid);
        setEntityResolver(configuration.getEntityResolver());
        setErrorHandler(configuration.getErrorHandler());
        SymbolTable sym = new SymbolTable(BIG_PRIME);
        XMLGrammarPoolImpl grammarPool = new XMLGrammarPoolImpl();
        XMLGrammarPreparser preparser = new XMLGrammarPreparser(sym);
        preparser.registerPreparser(XMLGrammarDescription.XML_DTD, this);
        preparser.setEntityResolver(getEntityResolver());
        preparser.setErrorHandler(getErrorHandler());
        preparser.setProperty(GRAMMAR_POOL, grammarPool);
        preparser.setFeature(NAMESPACES_FEATURE, true);
        preparser.setFeature(VALIDATION_FEATURE, true);
        preparser.preparseGrammar(XMLGrammarDescription.XML_DTD, is);
    }

    @Override
    public void externalEntityDecl(String name,
                                   XMLResourceIdentifier id,
                                   Augmentations augs)
        throws XNIException
    {
        if (!entityDeclNames.contains(name)) {
            super.externalEntityDecl(name, id, augs);
            entityDeclNames.add(name);
            final String systemId = id.getLiteralSystemId();
            final String publicId = id.getPublicId();
            final String mappedSystem =
                systemId == null ? null : getInclusionIds().get(systemId);
            final String mappedPublic =
                publicId == null ? null : getInclusionIds().get(publicId);
            if ("system".equals(mappedSystem))
                inclusionEntityNames.add(name);
            else if ("public".equals(mappedPublic))
                inclusionEntityNames.add(name);
        }
    }

    @Override
    public void startParameterEntity(String name,
                                     XMLResourceIdentifier identifier,
                                     String encoding, Augmentations augs)
        throws XNIException
    {
        super.startParameterEntity(name, identifier, encoding, augs);
        if (inclusionEntityNames.contains(name))
            ++entityDepth;
        entityStack.push(name);
    }

    @Override
    public void endParameterEntity(String name, Augmentations augs)
        throws XNIException
    {
        super.endParameterEntity(name, augs);
        if (inclusionEntityNames.contains(name))
            --entityDepth;
        entityStack.pop();
    }

    @Override
    public void attributeDecl(String elementName, String attributeName,
                              String type, String[] enumeration,
                              String defaultType,
                              XMLString defaultValue,
                              XMLString rawDefaultValue,
                              Augmentations augmentations)
        throws XNIException
    {
        super.attributeDecl(elementName, attributeName, type, enumeration,
                            defaultType, defaultValue,
                            rawDefaultValue, augmentations);
        String defaultString =
            defaultValue == null ? "" : defaultValue.toString();
        captureEntityNames(defaultString);
        String rawDefaultString =
            rawDefaultValue == null ? "" : rawDefaultValue.toString();
        captureEntityNames(rawDefaultString);
    }

    @Override
    public void internalEntityDecl(String name, XMLString text,
                                   XMLString nonNormalizedText,
                                   Augmentations augs)
        throws XNIException
    {
        super.internalEntityDecl(name, text, nonNormalizedText, augs);
        if (entityDepth > 0 && !name.startsWith("%")) {
            String textString = text == null ? "" : text.toString();
            String nonNormalizedTextString =
                nonNormalizedText == null ? "" : nonNormalizedText.toString();
            captureEntityNames(textString);
            captureEntityNames(nonNormalizedTextString);
            inclusionEntityNames.add(name);
        }
    }

    private void captureEntityNames(String text) {
        Matcher m = GE_REX.matcher(text);
        int i = 0;
        while (m.find(i)) {
            i = m.end();
            inclusionEntityNames.add(m.group(1));
        }
    }
}
