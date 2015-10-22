package com.kendallshaw.dtdnormalizer;

import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

public class DocumentSource implements XMLDocumentSource {

    private XMLParserConfiguration configuration;

    public DocumentSource(XMLParserConfiguration config) {
        configuration = config;
    }

    public XMLParserConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(XMLParserConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setDocumentHandler(XMLDocumentHandler h) {
        getConfiguration().setDocumentHandler(h);
    }

    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return getConfiguration().getDocumentHandler();
    }
}
