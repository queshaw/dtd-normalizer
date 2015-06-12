package com.kendallshaw.dtdnormalizer;

import java.io.File;
import java.io.IOException;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;

public class IdentifierResolver implements XMLEntityResolver {

	private final static CatalogManager CM = new CatalogManager();
	private Catalog catalog = null;

	protected IdentifierResolver() { }

	public IdentifierResolver(final String catalogPath) throws Exception {
		final File cp = new File(catalogPath);
		CM.setCatalogFiles(cp.toURI().toASCIIString());
		CM.setIgnoreMissingProperties(true);
		//CM.setVerbosity(10);
		catalog = CM.getCatalog();
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier id)
			throws XNIException, IOException {
		final String publicId = id.getPublicId();
		final String systemId = id.getExpandedSystemId();
		final String base = catalog.getCurrentBase();
		String resolved = null;
		if (publicId == null)
			resolved = catalog.resolveSystem(systemId);
		else
			resolved = catalog.resolvePublic(publicId, systemId);
		if (resolved == null)
			return new XMLInputSource(id);
		else
			return new XMLInputSource(publicId, resolved, base);
	}
}
