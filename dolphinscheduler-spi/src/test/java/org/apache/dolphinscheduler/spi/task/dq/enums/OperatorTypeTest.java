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

public class OperatorTypeTest {

    @Test
    public void testGetCode() {
        assertEquals(0, OperatorType.EQ.getCode());
        assertEquals(1, OperatorType.LT.getCode());
        assertEquals(2, OperatorType.LE.getCode());
        assertEquals(3, OperatorType.GT.getCode());
        assertEquals(4, OperatorType.GE.getCode());
        assertEquals(5, OperatorType.NE.getCode());
    }

    @Test
    public void testGetDescription() {
        assertEquals("equal", OperatorType.EQ.getDescription());
        assertEquals("little than", OperatorType.LT.getDescription());
        assertEquals("little and equal", OperatorType.LE.getDescription());
        assertEquals("great than", OperatorType.GT.getDescription());
        assertEquals("great and equal", OperatorType.GE.getDescription());
        assertEquals("not equal", OperatorType.NE.getDescription());
    }

    @Test
    public void testOf() {
        assertEquals(OperatorType.EQ, OperatorType.of(0));
        assertEquals(OperatorType.LT, OperatorType.of(1));
        assertEquals(OperatorType.LE, OperatorType.of(2));
        assertEquals(OperatorType.GT, OperatorType.of(3));
        assertEquals(OperatorType.GE, OperatorType.of(4));
        assertEquals(OperatorType.NE, OperatorType.of(5));
    }
}
