package org.apache.dolphinscheduler.plugin.task.api.parameters.resource;

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResourceParametersHelper {

    private Map<ResourceType, Map<Integer, AbstractResourceParameters>> map = new HashMap<>();

    public void put(ResourceType resourceType, Integer id) {
        put(resourceType, id, null);
    }

    public void put(ResourceType resourceType, Integer id, AbstractResourceParameters parameters) {
        Map<Integer, AbstractResourceParameters> resourceParametersMap = map.get(resourceType);
        if (Objects.isNull(resourceParametersMap)) {
            resourceParametersMap = new HashMap<>();
            map.put(resourceType, resourceParametersMap);
        }
        resourceParametersMap.put(id, parameters);
    }

    public Map<ResourceType, Map<Integer, AbstractResourceParameters>> getResourceMap() {
        return map;
    }

    public Map<Integer, AbstractResourceParameters> getResourceMap(ResourceType resourceType) {
        return this.getResourceMap().get(resourceType);
    }

    public AbstractResourceParameters getResourceParameters(ResourceType resourceType, Integer code) {
        return this.getResourceMap(resourceType).get(code);
    }
}
