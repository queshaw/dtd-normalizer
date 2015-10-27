package com.kendallshaw.dtdnormalizer;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;

public class ErrorHandler implements XMLErrorHandler {

	private Serialization logger;

	public ErrorHandler() {
	}

	public ErrorHandler(final Serialization log) {
		setLogger(log);
	}

	public Serialization getLogger() {
		return logger;
	}

	public void setLogger(final Serialization log) {
		logger = log;
	}

	@Override
	public void error(String domain, String key, XMLParseException e)
			throws XNIException {
        logError("error", key, e);
	}

	@Override
	public void fatalError(String domain, String key, XMLParseException e)
			throws XNIException {
        logError("fatalError", key, e);
        throw e;
	}

	@Override
	public void warning(String domain, String key, XMLParseException e)
			throws XNIException {
        logError("warning", key, e);
	}

    protected void logError(final String type, final String key,
    		                final XMLParseException e)
        throws XNIException
    {
		final Serialization l = getLogger();
		if (l == null) {
		    int line = e.getLineNumber();
		    int col = e.getColumnNumber();
		    StringBuilder sb = new StringBuilder();
		    sb.append(e.getExpandedSystemId());
		    if (line > -1) {
		        if (col > -1)
		            sb.append(String.format(" [%s:%s]", line, col));
		        else
                    sb.append(String.format(" [%s]", line));
		    }
		    sb.append(" " + e.getLocalizedMessage());
		    System.err.println(sb.toString());
		} else {
		    l.text(type);
		    l.text(" " + key);
		}
    }
}
