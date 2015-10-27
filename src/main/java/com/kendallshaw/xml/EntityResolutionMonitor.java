package com.kendallshaw.xml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class EntityResolutionMonitor implements EntityMonitor {

    private Map<EntityResourceIdentifier, Object> entities =
        new LinkedHashMap<EntityResourceIdentifier, Object>();

    public EntityResolutionMonitor() { }

    @Override
    public Set<EntityResourceIdentifier> entities() {
        return entities.keySet();
    }

    @Override
    public EntityResourceIdentifier resolveEntity(String publicId,
                                                  String systemId,
                                                  String baseSystemId,
                                                  String resolvedSystemId,
                                                  String charset)
    {
        ResolvedEntityIdentifier id =
                new ResolvedEntityIdentifier(publicId, systemId, charset);
              entities.put(id, "");
              return id;
    }

    @Override
    public EntityResourceIdentifier resolveEntity(String publicId,
                                                  String systemId,
                                                  String charset)
    {
        return resolveEntity(publicId, systemId, null, null, charset);
    }

    @Override
    public EntityResourceIdentifier resolveEntity(String systemId,
                                                  String charset)
    {
        return resolveEntity(null, systemId, charset);
    }
}
