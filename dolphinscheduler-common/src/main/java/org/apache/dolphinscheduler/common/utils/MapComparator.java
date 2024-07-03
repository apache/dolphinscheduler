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

package org.apache.dolphinscheduler.common.utils;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MapComparator<K, V> {

    private final Map<K, V> oldMap;
    private final Map<K, V> newMap;

    public MapComparator(Map<K, V> oldMap, Map<K, V> newMap) {
        this.oldMap = oldMap;
        this.newMap = newMap;
    }

    /**
     * Get keys that are in the new map but not in the old map
     */
    public Set<K> getKeysToAdd() {
        if (MapUtils.isEmpty(newMap)) {
            return SetUtils.emptySet();
        }
        if (MapUtils.isEmpty(oldMap)) {
            return new HashSet<>(newMap.keySet());
        }
        Set<K> keysToAdd = new HashSet<>(newMap.keySet());
        keysToAdd.removeAll(oldMap.keySet());
        return keysToAdd;
    }

    /**
     * Get values which key in the new map but not in the old map
     */
    public List<V> getValuesToAdd() {
        if (MapUtils.isEmpty(newMap)) {
            return Collections.emptyList();
        }
        return getKeysToAdd().stream().map(newMap::get).collect(Collectors.toList());
    }

    /**
     * Get keys which in the old map but not in the new map
     */
    public Set<K> getKeysToRemove() {
        if (MapUtils.isEmpty(oldMap)) {
            return SetUtils.emptySet();
        }
        if (MapUtils.isEmpty(newMap)) {
            return new HashSet<>(oldMap.keySet());
        }
        Set<K> keysToRemove = new HashSet<>(oldMap.keySet());
        keysToRemove.removeAll(newMap.keySet());
        return keysToRemove;
    }

    /**
     * Get values which key in the old map but not in the new map
     */
    public List<V> getValuesToRemove() {
        if (MapUtils.isEmpty(oldMap)) {
            return Collections.emptyList();
        }
        return getKeysToRemove().stream().map(oldMap::get).collect(Collectors.toList());
    }

    /**
     * Get keys which in both the old map and the new map, but the value is different
     */
    public Set<K> getKeysToUpdate() {
        if (MapUtils.isEmpty(oldMap) || MapUtils.isEmpty(newMap)) {
            return SetUtils.emptySet();
        }
        Set<K> keysToUpdate = new HashSet<>(newMap.keySet());
        keysToUpdate.retainAll(oldMap.keySet());
        keysToUpdate.removeIf(key -> newMap.get(key).equals(oldMap.get(key)));

        return keysToUpdate;
    }

    /**
     * Get new values which key in both the old map and the new map, but the value is different
     */
    public List<V> getNewValuesToUpdate() {
        if (MapUtils.isEmpty(oldMap) || MapUtils.isEmpty(newMap)) {
            return Collections.emptyList();
        }
        return getKeysToUpdate().stream().map(newMap::get).collect(Collectors.toList());
    }
}
