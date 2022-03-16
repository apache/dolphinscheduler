/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
