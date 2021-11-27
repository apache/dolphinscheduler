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

package org.apache.dolphinscheduler.spi.task.dq.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InputTypeTest {

    @Test
    public void testGetCode() {
        assertEquals(0, InputType.DEFAULT.getCode());
        assertEquals(1, InputType.STATISTICS.getCode());
        assertEquals(2, InputType.COMPARISON.getCode());
        assertEquals(3, InputType.CHECK.getCode());
    }

    @Test
    public void testGetDescription() {
        assertEquals("default", InputType.DEFAULT.getDescription());
        assertEquals("statistics", InputType.STATISTICS.getDescription());
        assertEquals("comparison", InputType.COMPARISON.getDescription());
        assertEquals("check", InputType.CHECK.getDescription());
    }

    @Test
    public void testOf() {
        assertEquals(InputType.DEFAULT, InputType.of(0));
        assertEquals(InputType.STATISTICS, InputType.of(1));
        assertEquals(InputType.COMPARISON, InputType.of(2));
        assertEquals(InputType.CHECK, InputType.of(3));
    }
}
