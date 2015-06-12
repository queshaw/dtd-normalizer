package com.kendallshaw.dtdnormalizer;

import org.apache.xerces.parsers.DTDConfiguration;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;

public class XniConfiguration extends DTDConfiguration {

    public XniConfiguration() { }

	public XniConfiguration(SymbolTable symbolTable) {
		super(symbolTable);
	}

	public XniConfiguration(SymbolTable symbolTable,
			XMLGrammarPool grammarPool) {
		super(symbolTable, grammarPool);
	}

	public XniConfiguration(SymbolTable symbolTable,
			XMLGrammarPool grammarPool, XMLComponentManager parentSettings) {
		super(symbolTable, grammarPool, parentSettings);
	}

	public void initialize() throws Exception {
		setFeature(EXTERNAL_GENERAL_ENTITIES, Boolean.TRUE);
		setFeature(EXTERNAL_PARAMETER_ENTITIES, Boolean.TRUE);
		final String dups =
            System.getProperty("containerguesser.duplicates.warnings");
		final boolean warn = "true".equals(dups) || "yes".equals(dups);
		setFeature(WARN_ON_DUPLICATE_ATTDEF, warn);
		setFeature(WARN_ON_DUPLICATE_ENTITYDEF, warn);
	}
}
