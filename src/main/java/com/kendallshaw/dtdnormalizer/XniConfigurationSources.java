package com.kendallshaw.dtdnormalizer;

import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.parser.XMLDTDContentModelSource;
import org.apache.xerces.xni.parser.XMLDTDSource;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

public class XniConfigurationSources implements XMLDocumentSource,
                                                XMLDTDSource,
                                                XMLDTDContentModelSource
{
    private XMLParserConfiguration configuration;

    private XMLDocumentSource documentSource = this;

    private XMLDTDSource dtdSource = this;

    private XMLDTDContentModelSource contentModelSource = this;

    public XniConfigurationSources() { }

    public XniConfigurationSources(XMLParserConfiguration configuration) {
        this.configuration = configuration;
    }

    public XMLParserConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(XMLParserConfiguration configuration) {
        this.configuration = configuration;
    }

    // XMLDTDSource interface

    public XMLDocumentSource getDocumentSource() {
        return documentSource;
    }

    public void setDocumentSource(XMLDocumentSource documentSource) {
        this.documentSource = documentSource;
    }

    public XMLDTDSource getDTDSource() {
        return dtdSource;
    }

    public void setDTDSource(XMLDTDSource dtdSource) {
        this.dtdSource = dtdSource;
    }

    public XMLDTDContentModelSource getDTDContentModelSource() {
        return contentModelSource;
    }

    public void setDTDContentModelSource(XMLDTDContentModelSource src) {
        this.contentModelSource = src;
    }

    @Override
    public XMLDTDHandler getDTDHandler() {
        return configuration.getDTDHandler();
    }

    @Override
    public void setDTDHandler(XMLDTDHandler h) {
        configuration.setDTDHandler(h);
    }

    // XMLDocumentSource interface

    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return getConfiguration().getDocumentHandler();
    }

    @Override
    public void setDocumentHandler(XMLDocumentHandler h) {
        getConfiguration().setDocumentHandler(h);
    }

    // XMLDTDContentModelSource interface

    @Override
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return getConfiguration().getDTDContentModelHandler();
    }

    @Override
    public void setDTDContentModelHandler(XMLDTDContentModelHandler h) {
        getConfiguration().setDTDContentModelHandler(h);
    }
}
