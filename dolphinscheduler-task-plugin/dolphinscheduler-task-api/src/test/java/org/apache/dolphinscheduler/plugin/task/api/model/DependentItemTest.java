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

package org.apache.dolphinscheduler.plugin.task.api.model;

import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependentType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DependentItemTest {

    @Test
    public void testFromKeyNormal() {
        DependentItem item = new DependentItem().fromKey("2001-123-day-today");
        Assertions.assertEquals(2001, item.getDefinitionCode());
        Assertions.assertEquals(123, item.getDepTaskCode());
        Assertions.assertEquals("day", item.getCycle());
        Assertions.assertEquals("today", item.getDateValue());
    }

    @Test
    public void testFromKeyWithNegativeDepTaskCode() {
        // depTaskCode is -1 (DEPENDENT_ALL_TASK_CODE), getKey() produces "2001--1-day-today"
        DependentItem item = new DependentItem().fromKey("2001--1-day-today");
        Assertions.assertEquals(2001, item.getDefinitionCode());
        Assertions.assertEquals(-1, item.getDepTaskCode());
        Assertions.assertEquals("day", item.getCycle());
        Assertions.assertEquals("today", item.getDateValue());
    }

    @Test
    public void testFromKeyWithInvalidFormat() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new DependentItem().fromKey("invalid-key"));
    }

    @Test
    public void testFromKeyWithTooManyParts() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new DependentItem().fromKey("1-2-3-4-5-6"));
    }

    @Test
    public void testGetKeyNormal() {
        DependentItem item = new DependentItem();
        item.setDefinitionCode(2001);
        item.setDepTaskCode(123);
        item.setCycle("day");
        item.setDateValue("today");
        Assertions.assertEquals("2001-123-day-today", item.getKey());
    }

    @Test
    public void testGetKeyWithNegativeDepTaskCode() {
        DependentItem item = new DependentItem();
        item.setDefinitionCode(2001);
        item.setDepTaskCode(-1);
        item.setCycle("day");
        item.setDateValue("today");
        Assertions.assertEquals("2001--1-day-today", item.getKey());
    }

    @Test
    public void testRoundTripNormal() {
        DependentItem item = new DependentItem();
        item.setDefinitionCode(2001);
        item.setDepTaskCode(123);
        item.setCycle("day");
        item.setDateValue("today");
        String key = item.getKey();
        DependentItem parsed = new DependentItem().fromKey(key);
        Assertions.assertEquals(item.getDefinitionCode(), parsed.getDefinitionCode());
        Assertions.assertEquals(item.getDepTaskCode(), parsed.getDepTaskCode());
        Assertions.assertEquals(item.getCycle(), parsed.getCycle());
        Assertions.assertEquals(item.getDateValue(), parsed.getDateValue());
    }

    @Test
    public void testRoundTripWithNegativeDepTaskCode() {
        DependentItem item = new DependentItem();
        item.setDefinitionCode(2001);
        item.setDepTaskCode(-1);
        item.setCycle("day");
        item.setDateValue("today");
        String key = item.getKey();
        DependentItem parsed = new DependentItem().fromKey(key);
        Assertions.assertEquals(item.getDefinitionCode(), parsed.getDefinitionCode());
        Assertions.assertEquals(item.getDepTaskCode(), parsed.getDepTaskCode());
        Assertions.assertEquals(item.getCycle(), parsed.getCycle());
        Assertions.assertEquals(item.getDateValue(), parsed.getDateValue());
    }
}
