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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class VarPoolUtils {

    public List<Property> deserializeVarPool(String varPoolJson) {
        return JSONUtils.toList(varPoolJson, Property.class);
    }

    /**
     * @see #mergeVarPool(List)
     */
    public String mergeVarPoolJsonString(List<String> varPoolJsons) {
        if (CollectionUtils.isEmpty(varPoolJsons)) {
            return null;
        }
        List<List<Property>> varPools = varPoolJsons.stream()
                .map(VarPoolUtils::deserializeVarPool)
                .collect(Collectors.toList());
        List<Property> finalVarPool = mergeVarPool(varPools);
        return JSONUtils.toJsonString(finalVarPool);
    }

    /**
     * Merge the given two varpools, and return the merged varpool.
     * If the two varpools have the same property({@link Property#getProp()} and {@link Property#getDirect()} is same), the value of the property in varpool2 will be used.
     * // todo: we may need to consider the datatype of the property
     */
    public List<Property> mergeVarPool(List<List<Property>> varPools) {
        if (CollectionUtils.isEmpty(varPools)) {
            return null;
        }
        if (varPools.size() == 1) {
            return varPools.get(0);
        }
        Map<String, Property> result = new HashMap<>();
        for (List<Property> varPool : varPools) {
            if (CollectionUtils.isEmpty(varPool)) {
                continue;
            }
            for (Property property : varPool) {
                if (!Direct.OUT.equals(property.getDirect())) {
                    log.info("The direct should be OUT in varPool, but got {}", property.getDirect());
                    continue;
                }
                result.put(property.getProp(), property);
            }
        }
        return new ArrayList<>(result.values());
    }

    public String subtractVarPoolJson(String varPool, List<String> subtractVarPool) {
        List<Property> varPoolList = deserializeVarPool(varPool);
        List<List<Property>> subtractVarPoolList = subtractVarPool.stream()
                .map(VarPoolUtils::deserializeVarPool)
                .collect(Collectors.toList());
        List<Property> finalVarPool = subtractVarPool(varPoolList, subtractVarPoolList);
        return JSONUtils.toJsonString(finalVarPool);
    }

    /**
     * Return the subtracted varpool, which key is in varPool but not in subtractVarPool.
     */
    public List<Property> subtractVarPool(List<Property> varPool, List<List<Property>> subtractVarPool) {
        if (CollectionUtils.isEmpty(varPool)) {
            return null;
        }
        if (CollectionUtils.isEmpty(subtractVarPool)) {
            return varPool;
        }
        Map<String, Property> subtractVarPoolMap = new HashMap<>();
        for (List<Property> properties : subtractVarPool) {
            for (Property property : properties) {
                subtractVarPoolMap.put(property.getProp(), property);
            }
        }
        List<Property> result = new ArrayList<>();
        for (Property property : varPool) {
            if (!subtractVarPoolMap.containsKey(property.getProp())) {
                result.add(property);
            }
        }
        return result;
    }

}
