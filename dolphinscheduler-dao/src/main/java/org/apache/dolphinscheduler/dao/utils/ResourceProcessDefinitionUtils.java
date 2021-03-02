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
package org.apache.dolphinscheduler.dao.utils;

import org.apache.dolphinscheduler.common.utils.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * resource process definition utils
 */
public class ResourceProcessDefinitionUtils {
    /**
     * get resource process map key is resource id,value is the set of process definition
     * @param list the map key is process definition id and value is resource_ids
     * @return resource process definition map
     */
    public static Map<Integer, Set<Long>> getResourceProcessDefinitionMap(List<Map<String, Object>> list) {
        Map<Long, String> map = new HashMap<>();

        // resourceId -> processDefinitionCodes
        Map<Integer, Set<Long>> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Map<String, Object> tempMap : list) {
                map.put((Long) tempMap.get("code"), (String)tempMap.get("resource_ids"));
            }
        }

        for (Map.Entry<Long, String> entry : map.entrySet()) {
            Long code = entry.getKey();
            String[] resourceIds = entry.getValue().split(",");
            Set<Integer> resourceIdSet = Arrays.stream(resourceIds).map(Integer::parseInt).collect(Collectors.toSet());
            for (Integer resourceId : resourceIdSet) {
                Set<Long> codeSet;
                if (result.containsKey(resourceId)) {
                    codeSet = result.get(resourceId);
                } else {
                    codeSet = new HashSet<>();
                }
                codeSet.add(code);
                result.put(resourceId, codeSet);
            }
        }
        
        return result;
    }
}
