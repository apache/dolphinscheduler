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

import static com.google.common.truth.Truth.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MapComparatorTest {

    @Test
    void getKeysToAdd() {
        MapComparator<String, String> mapComparator = getMapComparator();
        assertThat(mapComparator.getKeysToAdd()).containsExactly("key5");
    }

    @Test
    void getKeysToAdd_newMapIsNull() {
        MapComparator<String, String> mapComparator = getMapComparatorWithNewMapIsNull();
        assertThat(mapComparator.getKeysToAdd()).isEmpty();
    }

    @Test
    void getKeysToAdd_oldMapIsNull() {
        MapComparator<String, String> mapComparator = getMapComparatorWithOldMapIsNull();
        assertThat(mapComparator.getKeysToAdd()).containsExactly("key1", "key2", "key3", "key5");
    }

    @Test
    void getValuesToAdd() {
        MapComparator<String, String> mapComparator = getMapComparator();
        assertThat(mapComparator.getValuesToAdd()).containsExactly("map2_value5");
    }

    @Test
    void getValuesToAdd_newMapIsNull() {
        MapComparator<String, String> mapComparator = getMapComparatorWithNewMapIsNull();
        assertThat(mapComparator.getValuesToAdd()).isEmpty();
    }

    @Test
    void getValuesToAdd_oldMapIsNull() {
        MapComparator<String, String> mapComparator = getMapComparatorWithOldMapIsNull();
        assertThat(mapComparator.getValuesToAdd())
                .containsExactly("map2_value1", "map2_value2", "map2_value3", "map2_value5");
    }

    @Test
    void getKeysToRemove() {
        MapComparator<String, String> mapComparator = getMapComparator();
        assertThat(mapComparator.getKeysToRemove()).containsExactly("key4");
    }

    @Test
    void getKeysToRemove_newMapIsNull() {
        MapComparator<String, String> mapComparator = getMapComparatorWithNewMapIsNull();
        assertThat(mapComparator.getKeysToRemove()).containsExactly("key1", "key2", "key3", "key4");
    }

    @Test
    void getKeysToRemove_oldMapIsNull() {
        MapComparator<String, String> mapComparator = getMapComparatorWithOldMapIsNull();
        assertThat(mapComparator.getKeysToRemove()).isEmpty();
    }

    @Test
    void getValuesToRemove() {
        MapComparator<String, String> mapComparator = getMapComparator();
        assertThat(mapComparator.getValuesToRemove()).containsExactly("map1_value4");
    }

    @Test
    void getValuesToRemove_newMapIsNull() {
        MapComparator<String, String> mapComparator = getMapComparatorWithNewMapIsNull();
        assertThat(mapComparator.getValuesToRemove())
                .containsExactly("map1_value1", "map1_value2", "map1_value3", "map1_value4");
    }

    @Test
    void getValuesToRemove_oldMapIsNull() {
        MapComparator<String, String> mapComparator = getMapComparatorWithOldMapIsNull();
        assertThat(mapComparator.getValuesToRemove()).isEmpty();
    }

    @Test
    void getKeysToUpdate() {
        MapComparator<String, String> mapComparator = getMapComparator();
        assertThat(mapComparator.getKeysToUpdate()).containsExactly("key1", "key2", "key3");
    }

    @Test
    void getKeysToUpdate_newMapIsNull() {
        MapComparator<String, String> mapComparator = getMapComparatorWithNewMapIsNull();
        assertThat(mapComparator.getKeysToUpdate()).isEmpty();
    }

    @Test
    void getKeysToUpdate_oldMapIsNull() {
        MapComparator<String, String> mapComparator = getMapComparatorWithOldMapIsNull();
        assertThat(mapComparator.getKeysToUpdate()).isEmpty();
    }

    @Test
    void getNewValuesToUpdate() {
        MapComparator<String, String> mapComparator = getMapComparator();
        assertThat(mapComparator.getNewValuesToUpdate())
                .containsExactly("map2_value1", "map2_value2", "map2_value3");
    }

    @Test
    void getNewValuesToUpdate_newMapIsEmpty() {
        MapComparator<String, String> mapComparator = getMapComparatorWithNewMapIsNull();
        assertThat(mapComparator.getNewValuesToUpdate()).isEmpty();
    }

    @Test
    void getNewValuesToUpdate_oldMapIsEmpty() {
        MapComparator<String, String> mapComparator = getMapComparatorWithOldMapIsNull();
        assertThat(mapComparator.getNewValuesToUpdate()).isEmpty();
    }

    private MapComparator<String, String> getMapComparator() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("key1", "map1_value1");
        map1.put("key2", "map1_value2");
        map1.put("key3", "map1_value3");
        map1.put("key4", "map1_value4");
        Map<String, String> map2 = new HashMap<>();
        map2.put("key1", "map2_value1");
        map2.put("key2", "map2_value2");
        map2.put("key3", "map2_value3");
        map2.put("key5", "map2_value5");
        return new MapComparator<>(map1, map2);
    }

    private MapComparator<String, String> getMapComparatorWithNewMapIsNull() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("key1", "map1_value1");
        map1.put("key2", "map1_value2");
        map1.put("key3", "map1_value3");
        map1.put("key4", "map1_value4");
        return new MapComparator<>(map1, null);
    }

    private MapComparator<String, String> getMapComparatorWithOldMapIsNull() {
        Map<String, String> map2 = new HashMap<>();
        map2.put("key1", "map2_value1");
        map2.put("key2", "map2_value2");
        map2.put("key3", "map2_value3");
        map2.put("key5", "map2_value5");
        return new MapComparator<>(null, map2);
    }
}
