package com.kendallshaw.xml;

import java.util.Map;
import java.util.Set;

public interface EntityMonitor {

    Set<EntityResourceIdentifier> entities();

    EntityResourceIdentifier resolveEntity(String publicId, String systemId,
                                           String baseSystemId,
                                           String resolvedSystemId,
                                           String charset);

    EntityResourceIdentifier resolveEntity(String publicId, String systemId,
                                           String charset);

    EntityResourceIdentifier resolveEntity(String systemId, String charset);
}
