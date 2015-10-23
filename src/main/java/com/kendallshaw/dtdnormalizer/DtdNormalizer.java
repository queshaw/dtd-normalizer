package com.kendallshaw.dtdnormalizer;

import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class DtdNormalizer {

    private XniConfiguration configuration;

    private Serialization serialization;

    private IdentifierResolver identifierResolver;

    private ErrorHandler errorHandler;

    private InputSource input;

    private OutputIdentifier output;

    public DtdNormalizer(XniConfiguration configuration) {
        this.configuration = configuration;
    }

    public Serialization getSerialization() {
        return serialization;
    }

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    public IdentifierResolver getIdentifierResolver() {
        return identifierResolver;
    }

    public void setIdentifierResolver(IdentifierResolver identifierResolver) {
        this.identifierResolver = identifierResolver;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public InputSource getInput() {
        return input;
    }

    public void setInput(InputSource input) {
        this.input = input;
    }

    public OutputIdentifier getOutput() {
        return output;
    }

    public void setOutput(OutputIdentifier output) {
        this.output = output;
    }

    public void serialize() throws Exception {
        Serialization out = getSerialization();
        DtdHandler dtdHandler = new DtdHandler(out, configuration);
        final DocumentHandler documentHandler =
            new DocumentHandler(out, configuration);
        InputSource is = getInput();
        documentHandler.setBaseSystemId(is.getSystemId());
        configuration.setDocumentHandler(documentHandler);
        configuration.setEntityResolver(getIdentifierResolver());
        configuration.setErrorHandler(getErrorHandler());
        configuration.setDTDContentModelHandler(dtdHandler);
        configuration.setDTDHandler(dtdHandler);
        configuration.initialize();
        configuration.parse(getInput());
    }
}
