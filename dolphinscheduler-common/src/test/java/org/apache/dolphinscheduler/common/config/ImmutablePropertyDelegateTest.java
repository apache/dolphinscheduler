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

package org.apache.dolphinscheduler.common.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ImmutablePropertyDelegateTest {

    private static final ImmutablePropertyDelegate immutablePropertyDelegate = new ImmutablePropertyDelegate();

    @Test
    void get() {
        Assertions.assertNull(immutablePropertyDelegate.get("null"));
    }

    @Test
    void testGetDefaultValue() {
        Assertions.assertEquals("default", immutablePropertyDelegate.get("null", "default"));
    }

    @Test
    void getPropertyKeys() {
        Assertions.assertNotNull(immutablePropertyDelegate.getPropertyKeys());
    }

    @Test
    void getOptional() {
        Assertions.assertFalse(immutablePropertyDelegate.getOptional("null").isPresent());
    }

    @Test
    void getInt() {
        Assertions.assertEquals(1, immutablePropertyDelegate.getInt("int.property"));
    }

    @Test
    void getIntDefault() {
        Assertions.assertEquals(2, immutablePropertyDelegate.getInt("int2.property", 2));
    }

    @Test
    void getLong() {
        Assertions.assertEquals(1, immutablePropertyDelegate.getLong("long.property"));
    }

    @Test
    void getLongDefault() {
        Assertions.assertEquals(2, immutablePropertyDelegate.getLong("long2.property", 2L));
    }

    @Test
    void getDouble() {
        Assertions.assertEquals(1.1, immutablePropertyDelegate.getDouble("double.property"));
    }

    @Test
    void getDoubleDefault() {
        Assertions.assertEquals(2.2, immutablePropertyDelegate.getDouble("double2.property", 2.2d));
    }

    @Test
    void getBoolean() {
        Assertions.assertEquals(true, immutablePropertyDelegate.getBoolean("boolean.property"));
    }

    @Test
    void getBooleanDefault() {
        Assertions.assertEquals(false, immutablePropertyDelegate.getBoolean("boolean2.property", false));
    }

}
