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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MapComparator<K, V> {

    private final Map<K, V> oldMap;
    private final Map<K, V> newMap;

    public MapComparator(Map<K, V> oldMap, Map<K, V> newMap) {
        this.oldMap = oldMap;
        this.newMap = newMap;
    }

    public Set<K> getKeysToAdd() {
        Set<K> keysToAdd = new HashSet<>(newMap.keySet());
        keysToAdd.removeAll(oldMap.keySet());
        return keysToAdd;
    }

    public List<V> getValuesToAdd() {
        return getKeysToAdd().stream().map(newMap::get).collect(Collectors.toList());
    }

    public Set<K> getKeysToRemove() {
        Set<K> keysToRemove = new HashSet<>(oldMap.keySet());
        keysToRemove.removeAll(newMap.keySet());
        return keysToRemove;
    }

    public List<V> getValuesToRemove() {
        return getKeysToRemove().stream().map(oldMap::get).collect(Collectors.toList());
    }

    public Set<K> getKeysToUpdate() {
        Set<K> keysToUpdate = new HashSet<>(newMap.keySet());
        keysToUpdate.retainAll(oldMap.keySet());
        keysToUpdate.removeIf(key -> newMap.get(key).equals(oldMap.get(key)));

        return keysToUpdate;
    }

    public List<V> getNewValuesToUpdate() {
        return getKeysToUpdate().stream().map(newMap::get).collect(Collectors.toList());
    }
}
